package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryAuditCompensationApplicationService {

    private static final InventoryAuditOperatorType REPLAY_OPERATOR_TYPE = InventoryAuditOperatorType.MANUAL;

    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;
    private final InventoryAuditReplayTransactionExecutor inventoryAuditReplayTransactionService;

    public InventoryAuditCompensationApplicationService(InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository,
                                             InventoryAuditReplayTransactionExecutor inventoryAuditReplayTransactionService) {
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
        this.inventoryAuditReplayTransactionService = inventoryAuditReplayTransactionService;
    }

    public InventoryAuditReplayResultDTO replayDeadLetter(TenantId tenantId, DeadLetterId deadLetterId, String replayKey,
                                                          OperatorId operatorId) {
        InventoryAuditDeadLetter deadLetter = inventoryAuditDeadLetterRepository.findAuditDeadLetterById(deadLetterId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND,
                        "dead-letter-not-found:" + deadLetterId));
        if (!Objects.equals(tenantId, deadLetter.getTenantId())) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN, "dead-letter-tenant-mismatch");
        }
        if (InventoryAuditReplayStatus.SUCCEEDED.equals(deadLetter.getReplayStatus())) {
            return new InventoryAuditReplayResultDTO(deadLetterId.value(), deadLetter.getReplayStatusValue(), deadLetter.getReplayKey(),
                    "already-replayed");
        }
        String resolvedReplayKey = resolveReplayKey(deadLetter, replayKey);
        Instant replayAt = Instant.now();
        // 回放前先认领死信，确保同一条死信在人工操作和后台任务并发时只会有一个执行者真正进入事务。
        boolean claimed = inventoryAuditDeadLetterRepository.claimAuditDeadLetterForReplay(deadLetterId, tenantId,
                resolvedReplayKey, REPLAY_OPERATOR_TYPE, operatorId, replayAt);
        if (!claimed) {
            return new InventoryAuditReplayResultDTO(deadLetterId.value(), InventoryAuditReplayStatus.FAILED.value(),
                    resolvedReplayKey, "dead-letter-not-claimable");
        }
        try {
            return inventoryAuditReplayTransactionService.replayClaimedDeadLetter(deadLetter, resolvedReplayKey,
                    REPLAY_OPERATOR_TYPE, operatorId, replayAt);
        } catch (RuntimeException txException) {
            String truncatedError = truncateError(txException.getMessage());
            Metrics.counter("bacon.inventory.audit.replay.tx.fail.total").increment();
            log.error("ALERT inventory audit replay tx failed, deadLetterId={}, replayKey={}",
                    deadLetterId, resolvedReplayKey, txException);
            try {
                // 事务层抛异常后，再走单独的补偿事务把死信状态和失败审计补全，避免原事务整体回滚后没有追踪痕迹。
                inventoryAuditReplayTransactionService.compensateReplayTxFailure(deadLetter, resolvedReplayKey,
                        REPLAY_OPERATOR_TYPE, operatorId, replayAt, truncatedError);
                Metrics.counter("bacon.inventory.audit.replay.tx.compensate.success.total").increment();
            } catch (RuntimeException compensateException) {
                Metrics.counter("bacon.inventory.audit.replay.tx.compensate.fail.total").increment();
                log.error("ALERT inventory audit replay tx compensate failed, deadLetterId={}, replayKey={}",
                        deadLetterId, resolvedReplayKey, compensateException);
            }
            return new InventoryAuditReplayResultDTO(deadLetterId.value(), InventoryAuditReplayStatus.FAILED.value(),
                    resolvedReplayKey, "tx-failed:" + truncatedError);
        }
    }

    public List<InventoryAuditReplayResultDTO> replayDeadLettersBatch(TenantId tenantId, List<DeadLetterId> deadLetterIds,
                                                                      String replayKeyPrefix, OperatorId operatorId) {
        if (deadLetterIds == null || deadLetterIds.isEmpty()) {
            return List.of();
        }
        // 批量回放本质是对单条回放的串行包装，保证每条死信的认领、事务和失败结果都独立结算。
        List<InventoryAuditReplayResultDTO> results = new ArrayList<>(deadLetterIds.size());
        for (DeadLetterId deadLetterId : deadLetterIds) {
            String replayKey = replayKeyPrefix == null || replayKeyPrefix.isBlank()
                    ? null
                    : replayKeyPrefix + "-" + deadLetterId.value();
            results.add(replayDeadLetter(tenantId, deadLetterId, replayKey, operatorId));
        }
        return List.copyOf(results);
    }

    private String resolveReplayKey(InventoryAuditDeadLetter deadLetter, String replayKey) {
        if (replayKey != null && !replayKey.isBlank()) {
            return replayKey;
        }
        int replayCount = deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount();
        return "DLQ-" + deadLetter.getOutboxIdValue() + "-R" + (replayCount + 1);
    }

    private String truncateError(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
