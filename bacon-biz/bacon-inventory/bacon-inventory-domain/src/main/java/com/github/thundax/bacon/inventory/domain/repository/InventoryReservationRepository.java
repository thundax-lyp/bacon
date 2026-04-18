package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import java.util.Optional;

public interface InventoryReservationRepository {

    InventoryReservation insert(InventoryReservation reservation);

    InventoryReservation update(InventoryReservation reservation);

    Optional<InventoryReservation> findByOrderNo(OrderNo orderNo);
}
