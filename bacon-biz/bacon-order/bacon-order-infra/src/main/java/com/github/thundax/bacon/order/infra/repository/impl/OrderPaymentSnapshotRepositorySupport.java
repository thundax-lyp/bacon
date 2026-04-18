package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderPaymentSnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderPaymentSnapshotMapper;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class OrderPaymentSnapshotRepositorySupport {

    private static final String PAYMENT_SNAPSHOT_ID_BIZ_TAG = "order_payment_snapshot_id";

    private final OrderPaymentSnapshotMapper orderPaymentSnapshotMapper;
    private final OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler;
    private final IdGenerator idGenerator;

    public OrderPaymentSnapshotRepositorySupport(
            OrderPaymentSnapshotMapper orderPaymentSnapshotMapper,
            OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler,
            IdGenerator idGenerator) {
        this.orderPaymentSnapshotMapper = orderPaymentSnapshotMapper;
        this.orderPaymentSnapshotPersistenceAssembler = orderPaymentSnapshotPersistenceAssembler;
        this.idGenerator = idGenerator;
    }

    public void insert(OrderPaymentSnapshot snapshot) {
        BaconContextHolder.requireTenantId();
        OrderPaymentSnapshotDO dataObject = orderPaymentSnapshotPersistenceAssembler.toDataObject(snapshot);
        dataObject.setId(idGenerator.nextId(PAYMENT_SNAPSHOT_ID_BIZ_TAG));
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        orderPaymentSnapshotMapper.insert(dataObject);
    }

    public void update(OrderPaymentSnapshot snapshot) {
        BaconContextHolder.requireTenantId();
        OrderPaymentSnapshotDO existing =
                orderPaymentSnapshotMapper.selectOne(Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(
                                OrderPaymentSnapshotDO::getOrderId,
                                snapshot.orderId() == null
                                        ? null
                                        : snapshot.orderId().value()));
        OrderPaymentSnapshotDO dataObject = orderPaymentSnapshotPersistenceAssembler.toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        dataObject.setId(existing.getId());
        orderPaymentSnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderPaymentSnapshot> findByOrderId(OrderId orderId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(orderPaymentSnapshotMapper.selectOne(
                        Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                                .eq(OrderPaymentSnapshotDO::getOrderId, orderId == null ? null : orderId.value())))
                .map(orderPaymentSnapshotPersistenceAssembler::toDomain);
    }
}
