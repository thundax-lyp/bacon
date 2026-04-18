package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryStockRepository {

    Optional<Inventory> findInventory(SkuId skuId);

    List<Inventory> findInventories();

    List<Inventory> findInventories(Set<SkuId> skuIds);

    List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize);

    long count(SkuId skuId, InventoryStatus status);

    Inventory insertInventory(Inventory inventory);

    Inventory updateInventory(Inventory inventory);
}
