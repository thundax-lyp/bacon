package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditDeadLetterRepository {

    default void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
    }

    default List<InventoryAuditDeadLetter> pageAuditDeadLetters(TenantId tenantId, OrderNo orderNo,
                                                                 String replayStatus, int pageNo, int pageSize) {
        return List.of();
    }

    default long countAuditDeadLetters(TenantId tenantId, OrderNo orderNo, String replayStatus) {
        return 0L;
    }

    default Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
        return Optional.empty();
    }

    default boolean claimAuditDeadLetterForReplay(DeadLetterId id, TenantId tenantId, String replayKey,
                                                  String operatorType, OperatorId operatorId, Instant replayAt) {
        return false;
    }

    default void markAuditDeadLetterReplaySuccess(DeadLetterId id, String replayKey, String operatorType, OperatorId operatorId,
                                                  Instant replayAt) {
    }

    default void markAuditDeadLetterReplayFailed(DeadLetterId id, String replayKey, String operatorType, OperatorId operatorId,
                                                 String replayError, Instant replayAt) {
    }
}
