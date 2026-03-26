package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InventoryRepositorySupport.class)
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InventoryRepositorySupport support;

    public InventoryReservationRepositoryImpl(InventoryRepositorySupport support) {
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
