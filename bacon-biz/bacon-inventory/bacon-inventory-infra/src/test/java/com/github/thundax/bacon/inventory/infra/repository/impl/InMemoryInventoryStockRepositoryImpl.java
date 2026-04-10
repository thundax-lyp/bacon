package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
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
        return support.findInventory(null, skuId);
    }

    @Override
    public List<Inventory> findInventories() {
        return support.findInventories(null);
    }

    @Override
    public List<Inventory> findInventories(Set<SkuId> skuIds) {
        return support.findInventories(null, skuIds);
    }

    @Override
    public List<Inventory> pageInventories(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
        return support.pageInventories(null, skuId, status, pageNo, pageSize);
    }

    @Override
    public long countInventories(SkuId skuId, InventoryStatus status) {
        return support.countInventories(null, skuId, status);
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return support.saveInventory(inventory);
    }
}
