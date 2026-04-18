package com.github.thundax.bacon.order.application.executor;

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
        // 定时任务只负责扫描过期 PROCESSING 记录，并通过领域规则把它们转为 FAILED，不直接重放业务动作。
        for (var record : orderIdempotencyRepository.listExpiredProcessing(now)) {
            record.expire(RECOVER_MESSAGE, now);
            if (orderIdempotencyRepository.updateStatus(
                    record,
                    OrderIdempotencyStatus.PROCESSING,
                    now)) {
                recovered++;
            }
        }
        if (recovered > 0) {
            log.warn("Recovered expired order idempotency processing records, count={}", recovered);
        }
    }
}
