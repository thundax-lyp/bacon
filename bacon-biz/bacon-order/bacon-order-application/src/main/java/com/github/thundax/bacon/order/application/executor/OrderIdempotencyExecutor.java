package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.application.codec.OrderIdempotencyRecordKeyCodec;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
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
        Instant now = Instant.now();
        String owner = applicationName + ":" + processingOwner;
        Instant leaseUntil = now.plusSeconds(Math.max(leaseSeconds, 1L));
        OrderIdempotencyRecordKey key = OrderIdempotencyRecordKeyCodec.toDomain(tenantId, orderNo, eventType);
        OrderIdempotencyRecord record = OrderIdempotencyRecord.create(key, owner, leaseUntil, now);
        // 先尝试插入 PROCESSING 记录，天然覆盖“首次执行”路径；失败后再分流到重复成功、仍在处理、失败重试三类情况。
        if (!orderIdempotencyRepository.createProcessing(record)) {
            if (skipForDuplicateSuccessOrProcessing(key, now)) {
                return;
            }
            boolean claimed = tryClaim(key, owner, leaseUntil, now);
            if (!claimed && skipForDuplicateSuccessOrProcessing(key, now)) {
                return;
            }
            if (!claimed) {
                throw new IllegalStateException("Failed to claim idempotency record for retry: " + eventType);
            }
        }
        try {
            action.run();
            // 业务动作执行完成后再标记 SUCCESS，避免“动作未完成但状态已成功”的幂等假阳性。
            if (!orderIdempotencyRepository.markSuccess(key, Instant.now())) {
                throw new IllegalStateException("Failed to mark idempotency success: " + eventType);
            }
        } catch (RuntimeException ex) {
            // 失败状态保留在幂等表里，供后续显式重试或租约恢复逻辑接管。
            orderIdempotencyRepository.markFailed(key, ex.getMessage(), Instant.now());
            throw ex;
        }
    }

    private boolean tryClaim(OrderIdempotencyRecordKey key, String owner, Instant leaseUntil, Instant now) {
        Optional<OrderIdempotencyRecord> existing = orderIdempotencyRepository.findByBusinessKey(key);
        if (existing.isEmpty()) {
            return false;
        }
        OrderIdempotencyStatus status = existing.get().getStatus();
        // FAILED 允许显式重试重新抢占；PROCESSING 只有租约过期后才允许别的节点接管。
        if (status == OrderIdempotencyStatus.FAILED) {
            return orderIdempotencyRepository.retryFromFailed(key, owner, leaseUntil, now, now);
        }
        if (status == OrderIdempotencyStatus.PROCESSING && isLeaseExpired(existing.get(), now)) {
            return orderIdempotencyRepository.claimExpiredProcessing(key, owner, leaseUntil, now, now);
        }
        return false;
    }

    private boolean skipForDuplicateSuccessOrProcessing(OrderIdempotencyRecordKey key, Instant now) {
        Optional<OrderIdempotencyRecord> existing = orderIdempotencyRepository.findByBusinessKey(key);
        if (existing.isEmpty()) {
            return false;
        }
        OrderIdempotencyStatus status = existing.get().getStatus();
        // SUCCESS 直接跳过；未过期的 PROCESSING 也跳过，避免并发节点重复执行业务动作。
        if (status == OrderIdempotencyStatus.SUCCESS) {
            return true;
        }
        return status == OrderIdempotencyStatus.PROCESSING && !isLeaseExpired(existing.get(), now);
    }

    private boolean isLeaseExpired(OrderIdempotencyRecord record, Instant now) {
        return record.getLeaseUntil() == null || !record.getLeaseUntil().isAfter(now);
    }
}
