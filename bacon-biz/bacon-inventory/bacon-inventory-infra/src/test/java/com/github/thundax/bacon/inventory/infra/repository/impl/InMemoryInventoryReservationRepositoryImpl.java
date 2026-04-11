package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
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
    public Optional<InventoryReservation> findReservation(OrderNo orderNo) {
        return support.findReservation(orderNo);
    }
}
