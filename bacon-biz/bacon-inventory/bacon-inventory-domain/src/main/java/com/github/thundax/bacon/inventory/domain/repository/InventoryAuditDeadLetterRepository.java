package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditDeadLetterRepository {

    default void insert(InventoryAuditDeadLetter deadLetter) {}

    default List<InventoryAuditDeadLetter> page(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
        return List.of();
    }

    default long count(OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
        return 0L;
    }

    default Optional<InventoryAuditDeadLetter> findById(DeadLetterId id) {
        return Optional.empty();
    }

    default boolean claimForReplay(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        return false;
    }

    default void markReplaySuccess(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {}

    default void markReplayFailed(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            String replayError,
            Instant replayAt) {}
}
