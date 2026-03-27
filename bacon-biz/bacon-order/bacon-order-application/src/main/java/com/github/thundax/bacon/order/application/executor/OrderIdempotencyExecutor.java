package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderIdempotencyExecutor {

    public static final String EVENT_MARK_PAID = "MARK_PAID";
    public static final String EVENT_MARK_PAYMENT_FAILED = "MARK_PAYMENT_FAILED";
    public static final String EVENT_CANCEL = "CANCEL";
    public static final String EVENT_CLOSE_EXPIRED = "CLOSE_EXPIRED";

    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final String processingOwner = UUID.randomUUID().toString();

    @Value("${bacon.order.idempotency.lease-seconds:60}")
    private long leaseSeconds;
    @Value("${spring.application.name:bacon-order}")
    private String applicationName;

    public OrderIdempotencyExecutor(OrderIdempotencyRepository orderIdempotencyRepository) {
        this.orderIdempotencyRepository = orderIdempotencyRepository;
    }

    public void execute(String eventType, Long tenantId, String orderNo, String paymentNo, Runnable action) {
        String normalizedPaymentNo = normalizePaymentNo(paymentNo);
        Instant now = Instant.now();
        String owner = applicationName + ":" + processingOwner;
        Instant leaseUntil = now.plusSeconds(Math.max(leaseSeconds, 1L));
        OrderIdempotencyRecord record = new OrderIdempotencyRecord();
        record.setTenantId(tenantId);
        record.setOrderNo(orderNo);
        record.setPaymentNo(normalizedPaymentNo);
        record.setEventType(eventType);
        record.setProcessingOwner(owner);
        record.setLeaseUntil(leaseUntil);
        record.setClaimedAt(now);
        // 先尝试插入 PROCESSING 记录，天然覆盖“首次执行”路径；失败后再分流到重复成功、仍在处理、失败重试三类情况。
        if (!orderIdempotencyRepository.createProcessing(record)) {
            if (skipForDuplicateSuccessOrProcessing(tenantId, orderNo, normalizedPaymentNo, eventType, now)) {
                return;
            }
            boolean claimed = tryClaim(tenantId, orderNo, normalizedPaymentNo, eventType, owner, leaseUntil, now);
            if (!claimed && skipForDuplicateSuccessOrProcessing(tenantId, orderNo, normalizedPaymentNo, eventType, now)) {
                return;
            }
            if (!claimed) {
                throw new IllegalStateException("Failed to claim idempotency record for retry: " + eventType);
            }
        }
        try {
            action.run();
            // 业务动作执行完成后再标记 SUCCESS，避免“动作未完成但状态已成功”的幂等假阳性。
            if (!orderIdempotencyRepository.markSuccess(tenantId, orderNo, normalizedPaymentNo, eventType, Instant.now())) {
                throw new IllegalStateException("Failed to mark idempotency success: " + eventType);
            }
        } catch (RuntimeException ex) {
            // 失败状态保留在幂等表里，供后续显式重试或租约恢复逻辑接管。
            orderIdempotencyRepository.markFailed(tenantId, orderNo, normalizedPaymentNo, eventType, ex.getMessage(),
                    Instant.now());
            throw ex;
        }
    }

    private boolean tryClaim(Long tenantId, String orderNo, String paymentNo, String eventType,
                             String owner, Instant leaseUntil, Instant now) {
        Optional<OrderIdempotencyRecord> existing = orderIdempotencyRepository.findByBusinessKey(tenantId, orderNo,
                paymentNo, eventType);
        if (existing.isEmpty()) {
            return false;
        }
        String status = existing.get().getStatus();
        // FAILED 允许显式重试重新抢占；PROCESSING 只有租约过期后才允许别的节点接管。
        if (OrderIdempotencyRecord.STATUS_FAILED.equals(status)) {
            return orderIdempotencyRepository.retryFromFailed(tenantId, orderNo, paymentNo, eventType,
                    owner, leaseUntil, now, now);
        }
        if (OrderIdempotencyRecord.STATUS_PROCESSING.equals(status) && isLeaseExpired(existing.get(), now)) {
            return orderIdempotencyRepository.claimExpiredProcessing(tenantId, orderNo, paymentNo, eventType,
                    owner, leaseUntil, now, now);
        }
        return false;
    }

    private boolean skipForDuplicateSuccessOrProcessing(Long tenantId, String orderNo, String paymentNo, String eventType,
                                                        Instant now) {
        Optional<OrderIdempotencyRecord> existing = orderIdempotencyRepository.findByBusinessKey(tenantId, orderNo,
                paymentNo, eventType);
        if (existing.isEmpty()) {
            return false;
        }
        String status = existing.get().getStatus();
        // SUCCESS 直接跳过；未过期的 PROCESSING 也跳过，避免并发节点重复执行业务动作。
        if (OrderIdempotencyRecord.STATUS_SUCCESS.equals(status)) {
            return true;
        }
        return OrderIdempotencyRecord.STATUS_PROCESSING.equals(status) && !isLeaseExpired(existing.get(), now);
    }

    private boolean isLeaseExpired(OrderIdempotencyRecord record, Instant now) {
        return record.getLeaseUntil() == null || !record.getLeaseUntil().isAfter(now);
    }

    private String normalizePaymentNo(String paymentNo) {
        return paymentNo == null ? "" : paymentNo;
    }
}
