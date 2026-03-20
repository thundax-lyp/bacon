package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository {

    Optional<Inventory> findInventory(Long tenantId, Long skuId);

    List<Inventory> findInventories(Long tenantId, Set<Long> skuIds);

    InventoryReservation saveReservation(InventoryReservation reservation);

    Optional<InventoryReservation> findReservation(Long tenantId, String orderNo);
}
