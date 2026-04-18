package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderIdempotencyRecordPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderIdempotencyRecordDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderIdempotencyRecordMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class OrderIdempotencyRepositorySupport {

    private static final int LAST_ERROR_MAX_LENGTH = 512;

    private final OrderIdempotencyRecordMapper mapper;
    private final OrderIdempotencyRecordPersistenceAssembler orderIdempotencyRecordPersistenceAssembler;

    public OrderIdempotencyRepositorySupport(
            OrderIdempotencyRecordMapper mapper,
            OrderIdempotencyRecordPersistenceAssembler orderIdempotencyRecordPersistenceAssembler) {
        this.mapper = mapper;
        this.orderIdempotencyRecordPersistenceAssembler = orderIdempotencyRecordPersistenceAssembler;
    }

    public boolean insert(OrderIdempotencyRecord record) {
        OrderIdempotencyRecordDO dataObject = orderIdempotencyRecordPersistenceAssembler.toDataObject(record);
        try {
            // 幂等首执依赖唯一业务键插入；插入冲突不算异常，而是说明已经存在并发或历史记录。
            mapper.insert(dataObject);
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    public boolean updateStatus(OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus) {
        return updateStatus(record, currentStatus, null);
    }

    public boolean updateStatus(
            OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus, Instant leaseExpiredBefore) {
        OrderIdempotencyRecordDO dataObject = orderIdempotencyRecordPersistenceAssembler.toDataObject(record);
        var update = Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                .eq(OrderIdempotencyRecordDO::getOrderNo, dataObject.getOrderNo())
                .eq(OrderIdempotencyRecordDO::getEventType, dataObject.getEventType())
                .eq(OrderIdempotencyRecordDO::getStatus, currentStatus.value());
        if (leaseExpiredBefore != null) {
            update.and(wrapper -> wrapper.isNull(OrderIdempotencyRecordDO::getLeaseUntil)
                    .or()
                    .le(OrderIdempotencyRecordDO::getLeaseUntil, leaseExpiredBefore));
        }
        // 仓储只负责按 key + currentStatus(+可选租约前提) 做状态 CAS 持久化，不决定领域该迁移到哪个状态。
        return mapper.update(
                        null,
                        update
                                .set(OrderIdempotencyRecordDO::getStatus, dataObject.getStatus())
                                .set(OrderIdempotencyRecordDO::getAttemptCount, dataObject.getAttemptCount())
                                .set(OrderIdempotencyRecordDO::getLastError, truncate(dataObject.getLastError()))
                                .set(OrderIdempotencyRecordDO::getProcessingOwner, dataObject.getProcessingOwner())
                                .set(OrderIdempotencyRecordDO::getLeaseUntil, dataObject.getLeaseUntil())
                                .set(OrderIdempotencyRecordDO::getClaimedAt, dataObject.getClaimedAt())
                                .set(OrderIdempotencyRecordDO::getUpdatedAt, dataObject.getUpdatedAt()))
                > 0;
    }

    public Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<OrderIdempotencyRecordDO>lambdaQuery()
                        .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                        .eq(
                                OrderIdempotencyRecordDO::getOrderNo,
                                key.orderNo() == null ? null : key.orderNo().value())
                        .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())))
                .map(orderIdempotencyRecordPersistenceAssembler::toDomain);
    }

    public List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
        return mapper.selectList(Wrappers.<OrderIdempotencyRecordDO>lambdaQuery()
                        .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                        .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                        .le(OrderIdempotencyRecordDO::getLeaseUntil, now))
                .stream()
                .map(orderIdempotencyRecordPersistenceAssembler::toDomain)
                .toList();
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= LAST_ERROR_MAX_LENGTH ? value : value.substring(0, LAST_ERROR_MAX_LENGTH);
    }

    private Long requireTenantId() {
        return BaconContextHolder.requireTenantId();
    }
}
