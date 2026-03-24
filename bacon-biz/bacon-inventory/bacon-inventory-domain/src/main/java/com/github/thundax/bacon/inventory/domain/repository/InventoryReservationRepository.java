package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import java.util.Optional;

public interface InventoryReservationRepository {

    InventoryReservation saveReservation(InventoryReservation reservation);

    Optional<InventoryReservation> findReservation(Long tenantId, String orderNo);
}
