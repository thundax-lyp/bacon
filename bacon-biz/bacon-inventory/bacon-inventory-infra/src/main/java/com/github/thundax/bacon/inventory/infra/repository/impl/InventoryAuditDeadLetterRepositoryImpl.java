package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InventoryRepositorySupport.class)
public class InventoryAuditDeadLetterRepositoryImpl implements InventoryAuditDeadLetterRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditDeadLetterRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        support.saveAuditDeadLetter(deadLetter);
    }

    @Override
    public List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                String replayStatus, int pageNo, int pageSize) {
        return support.pageAuditDeadLetters(tenantId, orderNo, replayStatus, pageNo, pageSize);
    }

    @Override
    public long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        return support.countAuditDeadLetters(tenantId, orderNo, replayStatus);
    }

    @Override
    public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return support.findAuditDeadLetterById(id);
    }

    @Override
    public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                 String operatorType, Long operatorId, Instant replayAt) {
        return support.claimAuditDeadLetterForReplay(id, tenantId, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                 Instant replayAt) {
        support.markAuditDeadLetterReplaySuccess(id, replayKey, operatorType, operatorId, replayAt);
    }

    @Override
    public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                String replayError, Instant replayAt) {
        support.markAuditDeadLetterReplayFailed(id, replayKey, operatorType, operatorId, replayError, replayAt);
    }
}
