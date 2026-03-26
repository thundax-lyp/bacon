package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InMemoryInventoryRepositorySupport.class)
public class InMemoryInventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryReservationRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public InventoryReservation saveReservation(InventoryReservation reservation) {
        return support.saveReservation(reservation);
    }

    @Override
    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        return support.findReservation(tenantId, orderNo);
    }
}
