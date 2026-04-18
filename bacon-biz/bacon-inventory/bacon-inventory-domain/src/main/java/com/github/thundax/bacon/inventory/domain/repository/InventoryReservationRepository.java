package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import java.util.Optional;

public interface InventoryReservationRepository {

    InventoryReservation upsertReservation(InventoryReservation reservation);

    Optional<InventoryReservation> findReservation(OrderNo orderNo);
}
