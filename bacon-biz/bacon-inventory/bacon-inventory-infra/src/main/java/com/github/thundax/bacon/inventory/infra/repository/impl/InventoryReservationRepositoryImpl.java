package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryReservationRepositoryImpl implements InventoryReservationRepository {

    private final InventoryRepositorySupport support;

    public InventoryReservationRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public InventoryReservation insertReservation(InventoryReservation reservation) {
        return support.insertReservation(reservation);
    }

    @Override
    public InventoryReservation updateReservation(InventoryReservation reservation) {
        return support.updateReservation(reservation);
    }

    @Override
    public Optional<InventoryReservation> findReservation(OrderNo orderNo) {
        return support.findReservation(orderNo);
    }
}
