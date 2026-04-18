package com.github.thundax.bacon.order.application.saga;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderOutboxRetrier {

    private static final int MAX_EXPONENT = 20;
    private static final String DEAD_LETTER_ID_BIZ_TAG = "order_outbox_dead_letter_id";

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderOutboxDeadLetterRepository orderOutboxDeadLetterRepository;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;
    private final IdGenerator idGenerator;

    @Value("${bacon.order.outbox.retry.enabled:true}")
    private boolean enabled;

    @Value("${bacon.order.outbox.retry.batch-size:100}")
    private int batchSize;

    @Value("${bacon.order.outbox.retry.max-retries:6}")
    private int maxRetries;

    @Value("${bacon.order.outbox.retry.base-delay-seconds:30}")
    private long baseDelaySeconds;

    @Value("${bacon.order.outbox.retry.max-delay-seconds:1800}")
    private long maxDelaySeconds;

    @Value("${bacon.order.outbox.retry.lease-seconds:60}")
    private long leaseSeconds;

    @Value("${spring.application.name:bacon-order}")
    private String applicationName;

    private final String processingOwner = UUID.randomUUID().toString();

    public OrderOutboxRetrier(
            OrderOutboxRepository orderOutboxRepository,
            OrderOutboxDeadLetterRepository orderOutboxDeadLetterRepository,
            OrderOutboxActionExecutor orderOutboxActionExecutor,
            IdGenerator idGenerator) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.orderOutboxDeadLetterRepository = orderOutboxDeadLetterRepository;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
        this.idGenerator = idGenerator;
    }

    @Scheduled(fixedDelayString = "${bacon.order.outbox.retry.fixed-delay-ms:10000}")
    public void retryOutbox() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        // 先释放过期租约，再拉取一批可重试事件，避免节点异常退出后事件永久卡在 PROCESSING。
        orderOutboxRepository.releaseExpiredLease(now);
        int safeBatchSize = Math.max(batchSize, 1);
        Instant leaseUntil = now.plusSeconds(Math.max(leaseSeconds, 1L));
        String owner = applicationName + ":" + processingOwner;
        List<OrderOutboxEvent> events =
                orderOutboxRepository.claimRetryableOutbox(now, safeBatchSize, owner, leaseUntil);
        for (OrderOutboxEvent event : events) {
            retryOne(event, owner, now);
        }
    }

    private void retryOne(OrderOutboxEvent event, String owner, Instant now) {
        try {
            orderOutboxActionExecutor.executeClaimed(event);
            // 事件只有在业务动作真正执行成功后才删除，失败统一回到重试/死信分支。
            orderOutboxRepository.deleteClaimed(event.getId(), owner);
        } catch (RuntimeException ex) {
            handleRetryFailure(event, owner, now, ex);
        }
    }

    private void handleRetryFailure(OrderOutboxEvent event, String owner, Instant now, RuntimeException ex) {
        int nextRetryCount = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
        String message = truncate(ex.getMessage());
        // 超过重试上限后把事件移入死信表，后续只允许人工或专门补偿链路接手，不再由定时任务继续重试。
        if (nextRetryCount > maxRetries) {
            String deadReason = "MAX_RETRIES_EXCEEDED";
            if (orderOutboxRepository.markDeadClaimed(event.getId(), owner, nextRetryCount, deadReason, message, now)) {
                orderOutboxDeadLetterRepository.insertDeadLetter(OrderOutboxDeadLetter.create(
                        idGenerator.nextId(DEAD_LETTER_ID_BIZ_TAG),
                        event.getId() == null ? null : event.getId().value(),
                        event.getEventCode() == null
                                ? null
                                : event.getEventCode().value(),
                        event.getOrderNo() == null ? null : event.getOrderNo().value(),
                        event.getEventType(),
                        event.getBusinessKey(),
                        event.getPayload(),
                        nextRetryCount,
                        message,
                        deadReason,
                        now,
                        OrderOutboxReplayStatus.PENDING,
                        0,
                        null,
                        null,
                        now,
                        now));
                log.error(
                        "ALERT order outbox retry exhausted, outboxId={}, eventType={}, orderNo={}",
                        event.getId(),
                        event.getEventType() == null
                                ? null
                                : event.getEventType().value(),
                        event.getOrderNo() == null ? null : event.getOrderNo().value(),
                        ex);
            }
            return;
        }
        // 未到上限时按指数退避回写下一次重试时间，避免固定频率放大下游故障。
        Instant nextRetryAt = now.plusSeconds(nextDelaySeconds(nextRetryCount));
        orderOutboxRepository.markRetryingClaimed(event.getId(), owner, nextRetryCount, nextRetryAt, message, now);
        log.warn(
                "Order outbox retry failed, outboxId={}, eventType={}, orderNo={}, retryCount={}",
                event.getId(),
                event.getEventType() == null ? null : event.getEventType().value(),
                event.getOrderNo() == null ? null : event.getOrderNo().value(),
                nextRetryCount,
                ex);
    }

    private long nextDelaySeconds(int retryCount) {
        long normalizedBaseDelay = Math.max(baseDelaySeconds, 1L);
        long normalizedMaxDelay = Math.max(maxDelaySeconds, normalizedBaseDelay);
        int exponent = Math.min(Math.max(retryCount - 1, 0), MAX_EXPONENT);
        long computed = normalizedBaseDelay * (1L << exponent);
        return Math.min(computed, normalizedMaxDelay);
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
