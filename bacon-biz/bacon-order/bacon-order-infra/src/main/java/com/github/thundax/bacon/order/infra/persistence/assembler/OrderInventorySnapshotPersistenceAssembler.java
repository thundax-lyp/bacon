package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import org.springframework.stereotype.Component;

@Component
public class OrderInventorySnapshotPersistenceAssembler {

    public OrderInventorySnapshotDO toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDO(
                snapshot.id(),
                BaconContextHolder.requireTenantId(),
                snapshot.orderNo() == null ? null : snapshot.orderNo().value(),
                snapshot.reservationNo() == null
                        ? null
                        : snapshot.reservationNo().value(),
                snapshot.inventoryStatus() == null
                        ? null
                        : snapshot.inventoryStatus().value(),
                snapshot.warehouseCode() == null
                        ? null
                        : snapshot.warehouseCode().value(),
                snapshot.failureReason(),
                snapshot.updatedAt());
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
