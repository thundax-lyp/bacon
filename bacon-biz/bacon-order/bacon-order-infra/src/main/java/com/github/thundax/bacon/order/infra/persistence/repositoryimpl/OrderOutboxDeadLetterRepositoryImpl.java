package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(OrderOutboxRepositorySupport.class)
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
