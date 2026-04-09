package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;

public interface OrderOutboxDeadLetterRepository {

    default void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {}
}
