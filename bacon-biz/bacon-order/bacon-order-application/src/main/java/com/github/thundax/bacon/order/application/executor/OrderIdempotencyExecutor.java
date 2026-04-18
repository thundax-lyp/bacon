package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.application.codec.OrderIdempotencyRecordKeyCodec;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
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

    public void execute(String eventType, String orderNo, Runnable action) {
        Instant now = Instant.now();
        String owner = applicationName + ":" + processingOwner;
        Instant leaseUntil = now.plusSeconds(Math.max(leaseSeconds, 1L));
        OrderIdempotencyRecordKey key = OrderIdempotencyRecordKeyCodec.toDomain(orderNo, eventType);
        OrderIdempotencyRecord record = OrderIdempotencyRecord.create(key, owner, leaseUntil, now);
        // 首次执行的状态迁移先在领域对象内完成，再尝试插入，避免“由仓储决定进入哪个状态”。
        record.startProcessing(now);
        // 先尝试插入已进入 PROCESSING 的记录；失败后再分流到重复成功、仍在处理、失败重试三类情况。
        if (!orderIdempotencyRepository.insert(record)) {
            if (skipForDuplicateSuccessOrProcessing(key, now)) {
                return;
            }
            OrderIdempotencyRecord claimed = tryClaim(key, owner, leaseUntil, now);
            if (claimed == null && skipForDuplicateSuccessOrProcessing(key, now)) {
                return;
            }
            if (claimed == null) {
                throw new IllegalStateException("Failed to claim idempotency record for retry: " + eventType);
            }
            record = claimed;
        }
        try {
            action.run();
        } catch (RuntimeException ex) {
            // 失败状态保留在幂等表里，供后续显式重试或租约恢复逻辑接管。
            record.markFailed(ex.getMessage(), Instant.now());
            orderIdempotencyRepository.updateStatus(record, OrderIdempotencyStatus.PROCESSING);
            throw ex;
        }
        record.markSuccess(Instant.now());
        // 业务动作执行完成后再标记 SUCCESS，避免“动作未完成但状态已成功”的幂等假阳性。
        if (!orderIdempotencyRepository.updateStatus(record, OrderIdempotencyStatus.PROCESSING)) {
            throw new IllegalStateException("Failed to mark idempotency success: " + eventType);
        }
    }

    private OrderIdempotencyRecord tryClaim(OrderIdempotencyRecordKey key, String owner, Instant leaseUntil, Instant now) {
        return orderIdempotencyRepository.findByKey(key)
                .map(existing -> {
                    // FAILED 允许显式重试重新抢占；PROCESSING 只有租约过期后才允许别的节点接管。
                    if (existing.isFailed()) {
                        existing.recover(owner, leaseUntil, now, now);
                        return orderIdempotencyRepository.updateStatus(existing, OrderIdempotencyStatus.FAILED)
                                ? existing
                                : null;
                    }
                    if (existing.isProcessingAndLeaseExpired(now)) {
                        existing.claim(owner, leaseUntil, now, now);
                        return orderIdempotencyRepository.updateStatus(existing, OrderIdempotencyStatus.PROCESSING, now)
                                ? existing
                                : null;
                    }
                    return null;
                })
                .orElse(null);
    }

    private boolean skipForDuplicateSuccessOrProcessing(OrderIdempotencyRecordKey key, Instant now) {
        return orderIdempotencyRepository.findByKey(key)
                .map(existing -> {
                    // SUCCESS 直接跳过；未过期的 PROCESSING 也跳过，避免并发节点重复执行业务动作。
                    if (existing.isSuccess()) {
                        return true;
                    }
                    return existing.isProcessingAndLeaseActive(now);
                })
                .orElse(false);
    }
}
