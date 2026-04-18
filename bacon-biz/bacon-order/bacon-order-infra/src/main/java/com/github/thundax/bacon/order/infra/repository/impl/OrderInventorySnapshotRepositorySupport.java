package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderInventorySnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderInventorySnapshotMapper;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class OrderInventorySnapshotRepositorySupport {

    private final OrderInventorySnapshotMapper orderInventorySnapshotMapper;
    private final OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler;

    public OrderInventorySnapshotRepositorySupport(
            OrderInventorySnapshotMapper orderInventorySnapshotMapper,
            OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler) {
        this.orderInventorySnapshotMapper = orderInventorySnapshotMapper;
        this.orderInventorySnapshotPersistenceAssembler = orderInventorySnapshotPersistenceAssembler;
    }

    public void insert(OrderInventorySnapshot snapshot) {
        BaconContextHolder.requireTenantId();
        OrderInventorySnapshotDO dataObject = orderInventorySnapshotPersistenceAssembler.toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        orderInventorySnapshotMapper.insert(dataObject);
    }

    public void update(OrderInventorySnapshot snapshot) {
        BaconContextHolder.requireTenantId();
        OrderInventorySnapshotDO existing =
                orderInventorySnapshotMapper.selectOne(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(
                                OrderInventorySnapshotDO::getOrderNo,
                                snapshot.orderNo() == null ? null : snapshot.orderNo().value()));
        OrderInventorySnapshotDO dataObject = orderInventorySnapshotPersistenceAssembler.toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        dataObject.setId(existing.getId());
        orderInventorySnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderInventorySnapshot> findByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(
                        orderInventorySnapshotMapper.selectOne(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                                .eq(OrderInventorySnapshotDO::getOrderNo, orderNo == null ? null : orderNo.value())))
                .map(orderInventorySnapshotPersistenceAssembler::toDomain);
    }
}
