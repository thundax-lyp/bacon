package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import java.util.Optional;

public interface InventoryReservationRepository {

    InventoryReservation saveReservation(InventoryReservation reservation);

    Optional<InventoryReservation> findReservation(TenantId tenantId, OrderNo orderNo);
}
