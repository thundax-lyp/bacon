package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryAuditDeadLetterRepositoryImpl implements InventoryAuditDeadLetterRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditDeadLetterRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insertAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        support.insertAuditDeadLetter(deadLetter);
    }

    @Override
    public List<InventoryAuditDeadLetter> page(
            OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
        return support.page(orderNo, replayStatus, pageNo, pageSize);
    }

    @Override
    public long count(OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
        return support.count(orderNo, replayStatus);
    }

    @Override
    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
        return support.findAuditDeadLetterById(id);
    }

    @Override
    public boolean claimAuditDeadLetterForReplay(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        return support.claimAuditDeadLetterForReplay(id, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markAuditDeadLetterReplaySuccess(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        support.markAuditDeadLetterReplaySuccess(id, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markAuditDeadLetterReplayFailed(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            String replayError,
            Instant replayAt) {
        support.markAuditDeadLetterReplayFailed(id, replayKey, operatorType, operatorId, replayError, replayAt);
    }
}
