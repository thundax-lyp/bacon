package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import org.springframework.stereotype.Component;

@Component
public class OrderInventorySnapshotPersistenceAssembler {

    public OrderInventorySnapshotDO toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDO(
                snapshot.getId(),
                BaconContextHolder.requireTenantId(),
                snapshot.getOrderNo() == null ? null : snapshot.getOrderNo().value(),
                snapshot.getReservationNo() == null ? null : snapshot.getReservationNo().value(),
                snapshot.getInventoryStatus() == null ? null : snapshot.getInventoryStatus().value(),
                snapshot.getWarehouseCode() == null ? null : snapshot.getWarehouseCode().value(),
                snapshot.getFailureReason(),
                snapshot.getUpdatedAt());
    }

    public OrderInventorySnapshot toDomain(OrderInventorySnapshotDO dataObject) {
        return OrderInventorySnapshot.reconstruct(
                dataObject.getId(),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getInventoryStatus() == null ? null : InventoryStatus.from(dataObject.getInventoryStatus()),
                dataObject.getWarehouseCode() == null ? null : WarehouseCode.of(dataObject.getWarehouseCode()),
                dataObject.getFailureReason(),
                dataObject.getUpdatedAt());
    }
}
