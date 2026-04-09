package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryStockRepository {

    Optional<Inventory> findInventory(TenantId tenantId, SkuId skuId);

    List<Inventory> findInventories(TenantId tenantId);

    List<Inventory> findInventories(TenantId tenantId, Set<SkuId> skuIds);

    List<Inventory> pageInventories(TenantId tenantId, SkuId skuId, InventoryStatus status, int pageNo, int pageSize);

    long countInventories(TenantId tenantId, SkuId skuId, InventoryStatus status);

    Inventory saveInventory(Inventory inventory);
}
