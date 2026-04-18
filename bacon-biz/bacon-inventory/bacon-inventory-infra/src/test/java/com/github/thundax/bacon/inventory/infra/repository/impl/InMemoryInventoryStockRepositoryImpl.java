package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryInventoryStockRepositoryImpl implements InventoryStockRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryStockRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Inventory> findInventory(SkuId skuId) {
        return support.findInventory(currentTenantId(), skuId);
    }

    @Override
    public List<Inventory> findInventories() {
        return support.findInventories(currentTenantId());
    }

    @Override
    public List<Inventory> findInventories(Set<SkuId> skuIds) {
        return support.findInventories(currentTenantId(), skuIds);
    }

    @Override
    public List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
        return support.page(currentTenantId(), skuId, status, pageNo, pageSize);
    }

    @Override
    public long count(SkuId skuId, InventoryStatus status) {
        return support.count(currentTenantId(), skuId, status);
    }

    @Override
    public Inventory insertInventory(Inventory inventory) {
        return support.insertInventory(inventory);
    }

    @Override
    public Inventory updateInventory(Inventory inventory) {
        return support.updateInventory(inventory);
    }

    private TenantId currentTenantId() {
        return BaconIdContextHelper.currentTenantId();
    }
}
