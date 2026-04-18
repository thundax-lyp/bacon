package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryStockRepository {

    Optional<Inventory> findBySkuId(SkuId skuId);

    List<Inventory> list();

    List<Inventory> listBySkuIds(Set<SkuId> skuIds);

    List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize);

    long count(SkuId skuId, InventoryStatus status);

    Inventory insert(Inventory inventory);

    Inventory update(Inventory inventory);
}
