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

    public boolean insertProcessing(OrderIdempotencyRecord record) {
        OrderIdempotencyRecordDO dataObject = orderIdempotencyRecordPersistenceAssembler.toDataObject(record);
        Instant now = Instant.now();
        dataObject.setStatus(OrderIdempotencyStatus.PROCESSING.value());
        dataObject.setAttemptCount(dataObject.getAttemptCount() == null ? 1 : dataObject.getAttemptCount());
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        dataObject.setLastError(null);
        try {
            // 幂等首执依赖唯一业务键插入；插入冲突不算异常，而是说明已经存在并发或历史记录。
            mapper.insert(dataObject);
            record.setStatus(OrderIdempotencyStatus.from(dataObject.getStatus()));
            record.setAttemptCount(dataObject.getAttemptCount());
            record.setProcessingOwner(dataObject.getProcessingOwner());
            record.setLeaseUntil(dataObject.getLeaseUntil());
            record.setClaimedAt(dataObject.getClaimedAt());
            record.setCreatedAt(dataObject.getCreatedAt());
            record.setUpdatedAt(dataObject.getUpdatedAt());
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    public boolean claimExpiredProcessing(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        // 只有租约过期的 PROCESSING 记录才允许被重新认领，避免多个节点并发接管同一业务动作。
        return mapper.update(
                        null,
                        Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                                .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                                .eq(
                                        OrderIdempotencyRecordDO::getOrderNo,
                                        key.orderNo() == null
                                                ? null
                                                : key.orderNo().value())
                                .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())
                                .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                                .and(wrapper -> wrapper.isNull(OrderIdempotencyRecordDO::getLeaseUntil)
                                        .or()
                                        .le(OrderIdempotencyRecordDO::getLeaseUntil, claimedAt))
                                .set(OrderIdempotencyRecordDO::getProcessingOwner, processingOwner)
                                .set(OrderIdempotencyRecordDO::getLeaseUntil, leaseUntil)
                                .set(OrderIdempotencyRecordDO::getClaimedAt, claimedAt)
                                .set(OrderIdempotencyRecordDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public Optional<OrderIdempotencyRecord> findByBusinessKey(OrderIdempotencyRecordKey key) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<OrderIdempotencyRecordDO>lambdaQuery()
                        .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                        .eq(
                                OrderIdempotencyRecordDO::getOrderNo,
                                key.orderNo() == null ? null : key.orderNo().value())
                        .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())))
                .map(orderIdempotencyRecordPersistenceAssembler::toDomain);
    }

    public boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
        // 成功回写要求当前状态仍是 PROCESSING，确保只有真正拿到执行权的节点才能结束这条幂等记录。
        return mapper.update(
                        null,
                        Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                                .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                                .eq(
                                        OrderIdempotencyRecordDO::getOrderNo,
                                        key.orderNo() == null
                                                ? null
                                                : key.orderNo().value())
                                .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())
                                .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                                .set(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.SUCCESS.value())
                                .set(OrderIdempotencyRecordDO::getLastError, null)
                                .set(OrderIdempotencyRecordDO::getProcessingOwner, null)
                                .set(OrderIdempotencyRecordDO::getLeaseUntil, null)
                                .set(OrderIdempotencyRecordDO::getClaimedAt, null)
                                .set(OrderIdempotencyRecordDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
        // 失败同样只允许从 PROCESSING -> FAILED，避免旧节点把后来已成功的记录重新覆盖成失败。
        return mapper.update(
                        null,
                        Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                                .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                                .eq(
                                        OrderIdempotencyRecordDO::getOrderNo,
                                        key.orderNo() == null
                                                ? null
                                                : key.orderNo().value())
                                .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())
                                .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                                .set(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.FAILED.value())
                                .set(OrderIdempotencyRecordDO::getLastError, truncate(lastError))
                                .set(OrderIdempotencyRecordDO::getProcessingOwner, null)
                                .set(OrderIdempotencyRecordDO::getLeaseUntil, null)
                                .set(OrderIdempotencyRecordDO::getClaimedAt, null)
                                .set(OrderIdempotencyRecordDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public boolean recoverFromFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return recoverFromFailed(key, null, null, null, updatedAt);
    }

    public boolean recoverFromFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        // 从 FAILED 重新进入 PROCESSING 时会自增 attemptCount，用于区分首次执行和显式重试次数。
        return mapper.update(
                        null,
                        Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                                .eq(OrderIdempotencyRecordDO::getTenantId, requireTenantId())
                                .eq(
                                        OrderIdempotencyRecordDO::getOrderNo,
                                        key.orderNo() == null
                                                ? null
                                                : key.orderNo().value())
                                .eq(OrderIdempotencyRecordDO::getEventType, key.eventType())
                                .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.FAILED.value())
                                .set(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                                .setSql("attempt_count = attempt_count + 1")
                                .set(OrderIdempotencyRecordDO::getLastError, null)
                                .set(OrderIdempotencyRecordDO::getProcessingOwner, processingOwner)
                                .set(OrderIdempotencyRecordDO::getLeaseUntil, leaseUntil)
                                .set(OrderIdempotencyRecordDO::getClaimedAt, claimedAt)
                                .set(OrderIdempotencyRecordDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public int recoverExpiredProcessing(Instant now, String recoverMessage) {
        // 过期恢复不会直接改成 SUCCESS，而是统一转 FAILED，交回应用层决定是否再次重试。
        return mapper.update(
                null,
                Wrappers.<OrderIdempotencyRecordDO>lambdaUpdate()
                        .eq(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.PROCESSING.value())
                        .le(OrderIdempotencyRecordDO::getLeaseUntil, now)
                        .set(OrderIdempotencyRecordDO::getStatus, OrderIdempotencyStatus.FAILED.value())
                        .set(OrderIdempotencyRecordDO::getLastError, truncate(recoverMessage))
                        .set(OrderIdempotencyRecordDO::getProcessingOwner, null)
                        .set(OrderIdempotencyRecordDO::getLeaseUntil, null)
                        .set(OrderIdempotencyRecordDO::getClaimedAt, null)
                        .set(OrderIdempotencyRecordDO::getUpdatedAt, now));
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
