package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InventoryAuditReplayTransactionFacade {

    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;

    public InventoryAuditReplayTransactionFacade(InventoryAuditRecordRepository inventoryAuditRecordRepository,
                                                 InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository,
                                                 InventoryTransactionExecutor inventoryTransactionExecutor) {
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
    }

    public InventoryAuditReplayResultDTO replayClaimedDeadLetter(InventoryAuditDeadLetter deadLetter, String replayKey,
                                                                 String operatorType, Long operatorId, Instant replayAt) {
        return inventoryTransactionExecutor.executeInNewTransaction(() -> doReplay(deadLetter, replayKey, operatorType,
                operatorId, replayAt));
    }

    public void compensateReplayTxFailure(InventoryAuditDeadLetter deadLetter, String replayKey,
                                          String operatorType, Long operatorId, Instant replayAt, String error) {
        inventoryTransactionExecutor.executeInNewTransaction(() -> {
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplayFailed(deadLetter.getId(), replayKey, operatorType, operatorId,
                    error, replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), InventoryAuditLog.ACTION_AUDIT_REPLAY_FAILED,
                    operatorType, operatorId, replayAt));
            return null;
        });
    }

    private InventoryAuditReplayResultDTO doReplay(InventoryAuditDeadLetter deadLetter, String replayKey,
                                                   String operatorType, Long operatorId, Instant replayAt) {
        try {
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), deadLetter.getActionType(), deadLetter.getOperatorType(),
                    deadLetter.getOperatorId(), deadLetter.getOccurredAt()));
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplaySuccess(deadLetter.getId(), replayKey, operatorType,
                    operatorId, replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), InventoryAuditLog.ACTION_AUDIT_REPLAY_SUCCEEDED,
                    operatorType, operatorId, replayAt));
            return new InventoryAuditReplayResultDTO(deadLetter.getId(), InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED,
                    replayKey, "ok");
        } catch (RuntimeException ex) {
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplayFailed(deadLetter.getId(), replayKey, operatorType, operatorId,
                    truncateError(ex.getMessage()), replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantId(), deadLetter.getOrderNo(),
                    deadLetter.getReservationNo(), InventoryAuditLog.ACTION_AUDIT_REPLAY_FAILED,
                    operatorType, operatorId, replayAt));
            return new InventoryAuditReplayResultDTO(deadLetter.getId(), InventoryAuditDeadLetter.REPLAY_STATUS_FAILED,
                    replayKey, "failed:" + truncateError(ex.getMessage()));
        }
    }

    private String truncateError(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
