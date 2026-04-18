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
    public InventoryReservation insert(InventoryReservation reservation) {
        return support.insert(reservation);
    }

    @Override
    public InventoryReservation update(InventoryReservation reservation) {
        return support.update(reservation);
    }

    @Override
    public Optional<InventoryReservation> findByOrderNo(OrderNo orderNo) {
        return support.findByOrderNo(orderNo);
    }
}
