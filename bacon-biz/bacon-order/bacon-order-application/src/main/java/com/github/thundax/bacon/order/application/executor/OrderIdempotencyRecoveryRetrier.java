package com.github.thundax.bacon.order.application.executor;

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
        // 这里只做“卡死租约转 FAILED”的托底恢复，不直接重放业务动作，避免定时任务绕过正常幂等入口。
        int recovered = orderIdempotencyRepository.recoverExpired(Instant.now(), RECOVER_MESSAGE);
        if (recovered > 0) {
            log.warn("Recovered expired order idempotency processing records, count={}", recovered);
        }
    }
}
