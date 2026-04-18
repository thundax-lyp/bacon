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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryInventoryAuditDeadLetterRepositoryImpl implements InventoryAuditDeadLetterRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryAuditDeadLetterRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insert(InventoryAuditDeadLetter deadLetter) {
        support.insert(deadLetter);
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
    public Optional<InventoryAuditDeadLetter> findById(DeadLetterId id) {
        return support.findById(id);
    }

    @Override
    public boolean claimForReplay(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        return support.claimForReplay(id, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markReplaySuccess(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            Instant replayAt) {
        support.markReplaySuccess(id, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markReplayFailed(
            DeadLetterId id,
            String replayKey,
            InventoryAuditOperatorType operatorType,
            OperatorId operatorId,
            String replayError,
            Instant replayAt) {
        support.markReplayFailed(id, replayKey, operatorType, operatorId, replayError, replayAt);
    }
}
