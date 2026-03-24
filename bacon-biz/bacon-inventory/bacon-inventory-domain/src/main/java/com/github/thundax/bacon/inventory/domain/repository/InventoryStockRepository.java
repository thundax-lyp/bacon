package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryStockRepository {

    Optional<Inventory> findInventory(Long tenantId, Long skuId);

    List<Inventory> findInventories(Long tenantId);

    List<Inventory> findInventories(Long tenantId, Set<Long> skuIds);

    List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize);

    long countInventories(Long tenantId, Long skuId, String status);

    Inventory saveInventory(Inventory inventory);
}
