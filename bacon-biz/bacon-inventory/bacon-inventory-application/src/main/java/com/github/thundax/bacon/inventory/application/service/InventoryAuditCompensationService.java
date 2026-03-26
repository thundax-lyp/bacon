package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class InventoryAuditCompensationService {

    private static final String REPLAY_OPERATOR_TYPE = "MANUAL";

    private final InventoryLogRepository inventoryLogRepository;

    public InventoryAuditCompensationService(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public InventoryAuditReplayResultDTO replayDeadLetter(Long tenantId, Long deadLetterId, String replayKey, Long operatorId) {
        InventoryAuditDeadLetter deadLetter = inventoryLogRepository.findAuditDeadLetterById(deadLetterId)
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
        boolean claimed = inventoryLogRepository.claimAuditDeadLetterForReplay(deadLetterId, tenantId, resolvedReplayKey,
                REPLAY_OPERATOR_TYPE, operatorId, replayAt);
        if (!claimed) {
            return new InventoryAuditReplayResultDTO(deadLetterId, InventoryAuditDeadLetter.REPLAY_STATUS_FAILED,
                    resolvedReplayKey, "dead-letter-not-claimable");
        }
        try {
            inventoryLogRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), deadLetter.getActionType(), deadLetter.getOperatorType(),
                    deadLetter.getOperatorId(), deadLetter.getOccurredAt()));
            inventoryLogRepository.markAuditDeadLetterReplaySuccess(deadLetterId, resolvedReplayKey, REPLAY_OPERATOR_TYPE,
                    operatorId, replayAt);
            inventoryLogRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), "AUDIT_REPLAY_SUCCEEDED", REPLAY_OPERATOR_TYPE, operatorId, replayAt));
            return new InventoryAuditReplayResultDTO(deadLetterId, InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED,
                    resolvedReplayKey, "ok");
        } catch (RuntimeException ex) {
            inventoryLogRepository.markAuditDeadLetterReplayFailed(deadLetterId, resolvedReplayKey, REPLAY_OPERATOR_TYPE,
                    operatorId, truncateError(ex.getMessage()), replayAt);
            inventoryLogRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), "AUDIT_REPLAY_FAILED", REPLAY_OPERATOR_TYPE, operatorId, replayAt));
            return new InventoryAuditReplayResultDTO(deadLetterId, InventoryAuditDeadLetter.REPLAY_STATUS_FAILED,
                    resolvedReplayKey, "failed:" + truncateError(ex.getMessage()));
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
