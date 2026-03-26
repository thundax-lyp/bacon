package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OrderIdempotencyExecutor {

    public static final String EVENT_MARK_PAID = "MARK_PAID";
    public static final String EVENT_MARK_PAYMENT_FAILED = "MARK_PAYMENT_FAILED";
    public static final String EVENT_CANCEL = "CANCEL";
    public static final String EVENT_CLOSE_EXPIRED = "CLOSE_EXPIRED";

    private final OrderIdempotencyRepository orderIdempotencyRepository;

    public OrderIdempotencyExecutor(OrderIdempotencyRepository orderIdempotencyRepository) {
        this.orderIdempotencyRepository = orderIdempotencyRepository;
    }

    public void execute(String eventType, Long tenantId, String orderNo, String paymentNo, Runnable action) {
        String normalizedPaymentNo = normalizePaymentNo(paymentNo);
        OrderIdempotencyRecord record = new OrderIdempotencyRecord();
        record.setTenantId(tenantId);
        record.setOrderNo(orderNo);
        record.setPaymentNo(normalizedPaymentNo);
        record.setEventType(eventType);
        if (!orderIdempotencyRepository.createProcessing(record)) {
            if (skipForDuplicateSuccessOrProcessing(tenantId, orderNo, normalizedPaymentNo, eventType)) {
                return;
            }
            boolean claimed = orderIdempotencyRepository.retryFromFailed(tenantId, orderNo, normalizedPaymentNo,
                    eventType, Instant.now());
            if (!claimed && skipForDuplicateSuccessOrProcessing(tenantId, orderNo, normalizedPaymentNo, eventType)) {
                return;
            }
            if (!claimed) {
                throw new IllegalStateException("Failed to claim idempotency record for retry: " + eventType);
            }
        }
        try {
            action.run();
            if (!orderIdempotencyRepository.markSuccess(tenantId, orderNo, normalizedPaymentNo, eventType, Instant.now())) {
                throw new IllegalStateException("Failed to mark idempotency success: " + eventType);
            }
        } catch (RuntimeException ex) {
            orderIdempotencyRepository.markFailed(tenantId, orderNo, normalizedPaymentNo, eventType, ex.getMessage(),
                    Instant.now());
            throw ex;
        }
    }

    private boolean skipForDuplicateSuccessOrProcessing(Long tenantId, String orderNo, String paymentNo, String eventType) {
        Optional<OrderIdempotencyRecord> existing = orderIdempotencyRepository.findByBusinessKey(tenantId, orderNo,
                paymentNo, eventType);
        if (existing.isEmpty()) {
            return false;
        }
        String status = existing.get().getStatus();
        return OrderIdempotencyRecord.STATUS_SUCCESS.equals(status)
                || OrderIdempotencyRecord.STATUS_PROCESSING.equals(status);
    }

    private String normalizePaymentNo(String paymentNo) {
        return paymentNo == null ? "" : paymentNo;
    }
}
