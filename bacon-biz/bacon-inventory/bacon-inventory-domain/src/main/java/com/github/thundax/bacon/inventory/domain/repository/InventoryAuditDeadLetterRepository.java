package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditDeadLetterRepository {

    default void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
    }

    default List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                 String replayStatus, int pageNo, int pageSize) {
        return List.of();
    }

    default long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        return 0L;
    }

    default Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return Optional.empty();
    }

    default boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                  String operatorType, Long operatorId, Instant replayAt) {
        return false;
    }

    default void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                  Instant replayAt) {
    }

    default void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                 String replayError, Instant replayAt) {
    }
}
