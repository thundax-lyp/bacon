package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
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
public class InventoryAuditCompensationService {

    private static final String REPLAY_OPERATOR_TYPE = "MANUAL";

    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;
    private final InventoryAuditReplayTransactionFacade inventoryAuditReplayTransactionFacade;

    public InventoryAuditCompensationService(InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository,
                                             InventoryAuditReplayTransactionFacade inventoryAuditReplayTransactionFacade) {
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
        this.inventoryAuditReplayTransactionFacade = inventoryAuditReplayTransactionFacade;
    }

    public InventoryAuditReplayResultDTO replayDeadLetter(Long tenantId, Long deadLetterId, String replayKey, Long operatorId) {
        InventoryAuditDeadLetter deadLetter = inventoryAuditDeadLetterRepository.findAuditDeadLetterById(deadLetterId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND,
                        "dead-letter-not-found:" + deadLetterId));
        if (!Objects.equals(tenantId, deadLetter.getTenantId())) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN, "dead-letter-tenant-mismatch");
        }
        if (InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED.equals(deadLetter.getReplayStatus())) {
            return new InventoryAuditReplayResultDTO(deadLetterId, deadLetter.getReplayStatus(), deadLetter.getReplayKey(),
                    "already-replayed");
        }
        String resolvedReplayKey = resolveReplayKey(deadLetter, replayKey);
        Instant replayAt = Instant.now();
        boolean claimed = inventoryAuditDeadLetterRepository.claimAuditDeadLetterForReplay(deadLetterId, tenantId, resolvedReplayKey,
                REPLAY_OPERATOR_TYPE, operatorId, replayAt);
        if (!claimed) {
            return new InventoryAuditReplayResultDTO(deadLetterId, InventoryAuditDeadLetter.REPLAY_STATUS_FAILED,
                    resolvedReplayKey, "dead-letter-not-claimable");
        }
        try {
            return inventoryAuditReplayTransactionFacade.replayClaimedDeadLetter(deadLetter, resolvedReplayKey,
                    REPLAY_OPERATOR_TYPE, operatorId, replayAt);
        } catch (RuntimeException txException) {
            String truncatedError = truncateError(txException.getMessage());
            Metrics.counter("bacon.inventory.audit.replay.tx.fail.total").increment();
            log.error("ALERT inventory audit replay tx failed, deadLetterId={}, replayKey={}",
                    deadLetterId, resolvedReplayKey, txException);
            try {
                inventoryAuditReplayTransactionFacade.compensateReplayTxFailure(deadLetter, resolvedReplayKey,
                        REPLAY_OPERATOR_TYPE, operatorId, replayAt, truncatedError);
                Metrics.counter("bacon.inventory.audit.replay.tx.compensate.success.total").increment();
            } catch (RuntimeException compensateException) {
                Metrics.counter("bacon.inventory.audit.replay.tx.compensate.fail.total").increment();
                log.error("ALERT inventory audit replay tx compensate failed, deadLetterId={}, replayKey={}",
                        deadLetterId, resolvedReplayKey, compensateException);
            }
            return new InventoryAuditReplayResultDTO(deadLetterId, InventoryAuditDeadLetter.REPLAY_STATUS_FAILED,
                    resolvedReplayKey, "tx-failed:" + truncatedError);
        }
    }

    public List<InventoryAuditReplayResultDTO> replayDeadLettersBatch(Long tenantId, List<Long> deadLetterIds,
                                                                      String replayKeyPrefix, Long operatorId) {
        if (deadLetterIds == null || deadLetterIds.isEmpty()) {
            return List.of();
        }
        List<InventoryAuditReplayResultDTO> results = new ArrayList<>(deadLetterIds.size());
        for (Long deadLetterId : deadLetterIds) {
            String replayKey = replayKeyPrefix == null || replayKeyPrefix.isBlank()
                    ? null
                    : replayKeyPrefix + "-" + deadLetterId;
            results.add(replayDeadLetter(tenantId, deadLetterId, replayKey, operatorId));
        }
        return List.copyOf(results);
    }

    private String resolveReplayKey(InventoryAuditDeadLetter deadLetter, String replayKey) {
        if (replayKey != null && !replayKey.isBlank()) {
            return replayKey;
        }
        int replayCount = deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount();
        return "DLQ-" + deadLetter.getId() + "-R" + (replayCount + 1);
    }

    private String truncateError(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
