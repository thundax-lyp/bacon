package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryStockRepositoryImpl implements InventoryStockRepository {

    private final InventoryRepositorySupport support;

    public InventoryStockRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Inventory> findInventory(TenantId tenantId, SkuId skuId) {
        return support.findInventory(tenantId, skuId);
    }

    @Override
    public List<Inventory> findInventories(TenantId tenantId) {
        return support.findInventories(tenantId);
    }

    @Override
    public List<Inventory> findInventories(TenantId tenantId, Set<SkuId> skuIds) {
        return support.findInventories(tenantId, skuIds);
    }

    @Override
    public List<Inventory> pageInventories(TenantId tenantId, SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
        return support.pageInventories(tenantId, skuId, status, pageNo, pageSize);
    }

    @Override
    public long countInventories(TenantId tenantId, SkuId skuId, InventoryStatus status) {
        return support.countInventories(tenantId, skuId, status);
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return support.saveInventory(inventory);
    }
}
