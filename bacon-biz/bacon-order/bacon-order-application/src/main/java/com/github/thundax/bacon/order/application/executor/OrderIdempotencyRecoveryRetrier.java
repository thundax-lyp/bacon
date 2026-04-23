package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderIdempotencyRecoveryRetrier {

    private static final String RECOVER_MESSAGE = "LEASE_EXPIRED_AUTO_RECOVERED";

    private final OrderIdempotencyRepository orderIdempotencyRepository;

    @Value("${bacon.order.idempotency.recovery.enabled:true}")
    private boolean enabled;

    public OrderIdempotencyRecoveryRetrier(OrderIdempotencyRepository orderIdempotencyRepository) {
        this.orderIdempotencyRepository = orderIdempotencyRepository;
    }

    @Scheduled(fixedDelayString = "${bacon.order.idempotency.recovery.fixed-delay-ms:10000}")
    public void recoverExpired() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        int recovered = 0;
        // 定时任务没有请求租户上下文：先跨租户扫描，再按记录自身 tenantId 恢复上下文执行状态更新。
        for (var scopedRecord : orderIdempotencyRepository.listExpiredProcessingAcrossTenants(now)) {
            Long tenantId = scopedRecord.tenantId() == null ? null : scopedRecord.tenantId().value();
            boolean updated = BaconContextHolder.callWithTenantId(
                    tenantId,
                    () -> expireOne(scopedRecord.record(), now));
            if (updated) {
                recovered++;
            }
        }
        if (recovered > 0) {
            log.warn("Recovered expired order idempotency processing records, count={}", recovered);
        }
    }

    private boolean expireOne(OrderIdempotencyRecord record, Instant now) {
        record.expire(RECOVER_MESSAGE, now);
        return orderIdempotencyRepository.updateStatus(record, OrderIdempotencyStatus.PROCESSING, now);
    }
}
