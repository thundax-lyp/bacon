package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderOutboxDeadLetterRepositoryImpl implements OrderOutboxDeadLetterRepository {

    private final InMemoryOrderOutboxSupport support;

    public InMemoryOrderOutboxDeadLetterRepositoryImpl(InMemoryOrderOutboxSupport support) {
        this.support = support;
    }

    @Override
    public void insert(OrderOutboxDeadLetter deadLetter) {
        support.insert(deadLetter);
    }
}
