package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import com.github.thundax.bacon.order.infra.persistence.repository.impl.OrderOutboxRepositorySupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderOutboxDeadLetterRepositoryImpl implements OrderOutboxDeadLetterRepository {

    private final OrderOutboxRepositorySupport support;

    public OrderOutboxDeadLetterRepositoryImpl(OrderOutboxRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        support.saveDeadLetter(deadLetter);
    }
}
