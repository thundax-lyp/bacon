package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class OrderRepositoryTestConfiguration {

    @Bean
    @Primary
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }

    @Bean
    @Primary
    public OrderIdempotencyRepository orderIdempotencyRepository() {
        return Mockito.mock(OrderIdempotencyRepository.class);
    }

    @Bean
    @Primary
    public OrderOutboxRepository orderOutboxRepository() {
        return Mockito.mock(OrderOutboxRepository.class);
    }

    @Bean
    @Primary
    public OrderOutboxDeadLetterRepository orderOutboxDeadLetterRepository() {
        return Mockito.mock(OrderOutboxDeadLetterRepository.class);
    }
}
