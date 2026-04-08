package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InventoryAuditReplayTransactionExecutor {

    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository;
    private final InventoryTransactionExecutor inventoryTransactionExecutor;

    public InventoryAuditReplayTransactionExecutor(InventoryAuditRecordRepository inventoryAuditRecordRepository,
                                                  InventoryAuditDeadLetterRepository inventoryAuditDeadLetterRepository,
                                                  InventoryTransactionExecutor inventoryTransactionExecutor) {
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditDeadLetterRepository = inventoryAuditDeadLetterRepository;
        this.inventoryTransactionExecutor = inventoryTransactionExecutor;
    }

    public InventoryAuditReplayResultDTO replayClaimedDeadLetter(InventoryAuditDeadLetter deadLetter, String replayKey,
                                                                 String operatorType, OperatorId operatorId, Instant replayAt) {
        // 单条回放必须在新事务里执行，避免调用方已有事务把“补写审计 + 更新死信状态”一起拖进外层回滚。
        return inventoryTransactionExecutor.executeInNewTransaction(() -> doReplay(deadLetter, replayKey, operatorType,
                operatorId, replayAt));
    }

    public void compensateReplayTxFailure(InventoryAuditDeadLetter deadLetter, String replayKey,
                                          String operatorType, OperatorId operatorId, Instant replayAt, String error) {
        // 如果主回放事务直接失败，这里用补偿事务把死信状态显式改成 FAILED，并补一条失败审计日志。
        inventoryTransactionExecutor.executeInNewTransaction(() -> {
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplayFailed(DeadLetterId.of(deadLetter.getOutboxIdValue()),
                    replayKey, operatorType, operatorId,
                    error, replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantIdValue(), deadLetter.getOrderNoValue(),
                    deadLetter.getReservationNoValue(), InventoryAuditActionType.AUDIT_REPLAY_FAILED.value(),
                    operatorType, operatorId == null ? null : Long.valueOf(operatorId.value()), replayAt));
            return null;
        });
    }

    private InventoryAuditReplayResultDTO doReplay(InventoryAuditDeadLetter deadLetter, String replayKey,
                                                   String operatorType, OperatorId operatorId, Instant replayAt) {
        try {
            // 回放不是重放原业务动作，而是补写丢失的审计日志，并把死信改成已回放成功。
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantIdValue(), deadLetter.getOrderNoValue(),
                    deadLetter.getReservationNoValue(), deadLetter.getActionType().value(), deadLetter.getOperatorType().value(),
                    deadLetter.getOperatorIdValue(), deadLetter.getOccurredAt()));
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplaySuccess(DeadLetterId.of(deadLetter.getOutboxIdValue()),
                    replayKey, operatorType,
                    operatorId, replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantIdValue(), deadLetter.getOrderNoValue(),
                    deadLetter.getReservationNoValue(), InventoryAuditActionType.AUDIT_REPLAY_SUCCEEDED.value(),
                    operatorType, operatorId == null ? null : Long.valueOf(operatorId.value()), replayAt));
            return new InventoryAuditReplayResultDTO(deadLetter.getOutboxIdValue(), InventoryAuditReplayStatus.SUCCEEDED.value(),
                    replayKey, "ok");
        } catch (RuntimeException ex) {
            // 主事务内部已知失败也会就地写回 FAILED，保证调用方拿到失败结果时仓储状态已经一致。
            inventoryAuditDeadLetterRepository.markAuditDeadLetterReplayFailed(DeadLetterId.of(deadLetter.getOutboxIdValue()),
                    replayKey, operatorType, operatorId,
                    truncateError(ex.getMessage()), replayAt);
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, deadLetter.getTenantIdValue(), deadLetter.getOrderNoValue(),
                    deadLetter.getReservationNoValue(), InventoryAuditActionType.AUDIT_REPLAY_FAILED.value(),
                    operatorType, operatorId == null ? null : Long.valueOf(operatorId.value()), replayAt));
            return new InventoryAuditReplayResultDTO(deadLetter.getOutboxIdValue(), InventoryAuditReplayStatus.FAILED.value(),
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
