package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InMemoryOrderOutboxSupport.class)
public class InMemoryOrderOutboxDeadLetterRepositoryImpl implements OrderOutboxDeadLetterRepository {

    private final InMemoryOrderOutboxSupport support;

    public InMemoryOrderOutboxDeadLetterRepositoryImpl(InMemoryOrderOutboxSupport support) {
        this.support = support;
    }

    @Override
    public void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        support.saveDeadLetter(deadLetter);
    }
}
