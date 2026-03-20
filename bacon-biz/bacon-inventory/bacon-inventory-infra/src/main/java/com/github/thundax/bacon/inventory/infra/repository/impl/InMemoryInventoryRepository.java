package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
    private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();

    public InMemoryInventoryRepository() {
        inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, 1L, 100, 0, 100, "ENABLED", Instant.now()));
        inventories.put(key(1001L, 102L), new Inventory(2L, 1001L, 102L, 1L, 50, 0, 50, "ENABLED", Instant.now()));
    }

    @Override
    public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
        return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
    }

    @Override
    public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
        return skuIds.stream()
                .map(skuId -> inventories.get(key(tenantId, skuId)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public InventoryReservation saveReservation(InventoryReservation reservation) {
        reservations.put(reservationKey(reservation.getTenantId(), reservation.getOrderNo()), reservation);
        return reservation;
    }

    @Override
    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
    }

    private static String key(Long tenantId, Long skuId) {
        return tenantId + ":" + skuId;
    }

    private static String reservationKey(Long tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }
}
