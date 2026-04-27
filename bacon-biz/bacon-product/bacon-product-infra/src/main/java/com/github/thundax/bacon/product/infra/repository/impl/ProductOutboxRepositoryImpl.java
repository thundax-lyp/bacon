package com.github.thundax.bacon.product.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.enums.OutboxStatus;
import com.github.thundax.bacon.product.domain.repository.ProductOutboxRepository;
import com.github.thundax.bacon.product.infra.persistence.assembler.ProductPersistenceAssembler;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductOutboxDO;
import com.github.thundax.bacon.product.infra.persistence.mapper.ProductOutboxMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class ProductOutboxRepositoryImpl implements ProductOutboxRepository {

    private final ProductOutboxMapper mapper;
    private final ProductPersistenceAssembler assembler;

    public ProductOutboxRepositoryImpl(ProductOutboxMapper mapper, ProductPersistenceAssembler assembler) {
        this.mapper = mapper;
        this.assembler = assembler;
    }

    @Override
    public ProductOutbox save(ProductOutbox outbox) {
        mapper.insert(assembler.toDataObject(outbox, Instant.now()));
        return outbox;
    }

    @Override
    public ProductOutbox update(ProductOutbox outbox) {
        ProductOutboxDO dataObject = assembler.toDataObject(outbox, Instant.now());
        dataObject.setCreatedAt(null);
        mapper.updateById(dataObject);
        return outbox;
    }

    @Override
    public Optional<ProductOutbox> findById(Long eventId) {
        return Optional.ofNullable(mapper.selectById(eventId)).map(assembler::toOutbox);
    }

    public List<ProductOutbox> claimDue(Long tenantId, Instant now, int limit, String owner, Instant leaseUntil) {
        List<ProductOutboxDO> dueEvents = mapper.selectList(new LambdaQueryWrapper<ProductOutboxDO>()
                .eq(ProductOutboxDO::getTenantId, tenantId)
                .in(ProductOutboxDO::getOutboxStatus, OutboxStatus.PENDING.value(), OutboxStatus.FAILED.value())
                .and(wrapper -> wrapper.isNull(ProductOutboxDO::getNextRetryAt)
                        .or()
                        .le(ProductOutboxDO::getNextRetryAt, now))
                .orderByAsc(ProductOutboxDO::getCreatedAt)
                .last("limit " + limit));
        return dueEvents.stream()
                .filter(event -> claimOne(event.getId(), owner, leaseUntil, now))
                .map(event -> mapper.selectById(event.getId()))
                .map(assembler::toOutbox)
                .toList();
    }

    public boolean markRetry(Long eventId, String owner, Instant nextRetryAt, String failureReason) {
        return mapper.update(null, new LambdaUpdateWrapper<ProductOutboxDO>()
                        .eq(ProductOutboxDO::getId, eventId)
                        .eq(ProductOutboxDO::getProcessingOwner, owner)
                        .set(ProductOutboxDO::getOutboxStatus, OutboxStatus.FAILED.value())
                        .setSql("retry_count = retry_count + 1")
                        .set(ProductOutboxDO::getNextRetryAt, nextRetryAt)
                        .set(ProductOutboxDO::getProcessingOwner, null)
                        .set(ProductOutboxDO::getLeaseUntil, null)
                        .set(ProductOutboxDO::getFailureReason, failureReason)
                        .set(ProductOutboxDO::getUpdatedAt, Instant.now()))
                > 0;
    }

    public boolean markDead(Long eventId, String owner, String failureReason) {
        return mapper.update(null, new LambdaUpdateWrapper<ProductOutboxDO>()
                        .eq(ProductOutboxDO::getId, eventId)
                        .eq(ProductOutboxDO::getProcessingOwner, owner)
                        .set(ProductOutboxDO::getOutboxStatus, OutboxStatus.DEAD.value())
                        .set(ProductOutboxDO::getProcessingOwner, null)
                        .set(ProductOutboxDO::getLeaseUntil, null)
                        .set(ProductOutboxDO::getFailureReason, failureReason)
                        .set(ProductOutboxDO::getUpdatedAt, Instant.now()))
                > 0;
    }

    private boolean claimOne(Long eventId, String owner, Instant leaseUntil, Instant now) {
        return mapper.update(null, new LambdaUpdateWrapper<ProductOutboxDO>()
                        .eq(ProductOutboxDO::getId, eventId)
                        .in(ProductOutboxDO::getOutboxStatus, OutboxStatus.PENDING.value(), OutboxStatus.FAILED.value())
                        .set(ProductOutboxDO::getOutboxStatus, OutboxStatus.PROCESSING.value())
                        .set(ProductOutboxDO::getProcessingOwner, owner)
                        .set(ProductOutboxDO::getLeaseUntil, leaseUntil)
                        .set(ProductOutboxDO::getUpdatedAt, now))
                > 0;
    }
}
