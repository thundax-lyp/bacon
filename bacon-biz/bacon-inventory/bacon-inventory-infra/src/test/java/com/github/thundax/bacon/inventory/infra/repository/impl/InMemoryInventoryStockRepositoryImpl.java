package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InMemoryInventoryRepositorySupport.class)
public class InMemoryInventoryStockRepositoryImpl implements InventoryStockRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryStockRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return support.findInventory(tenantId, skuId);
    }

    @Override
    public List<Inventory> findInventories(Long tenantId) {
        return support.findInventories(tenantId);
    }

    @Override
    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        return support.findInventories(tenantId, skuIds);
    }

    @Override
    public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
        return support.pageInventories(tenantId, skuId, status, pageNo, pageSize);
    }

    @Override
    public long countInventories(Long tenantId, Long skuId, String status) {
        return support.countInventories(tenantId, skuId, status);
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return support.saveInventory(inventory);
    }
}
