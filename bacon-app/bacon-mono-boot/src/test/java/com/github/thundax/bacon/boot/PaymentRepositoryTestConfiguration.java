package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class PaymentRepositoryTestConfiguration {

    @Bean
    @Primary
    public PaymentOrderRepository paymentOrderRepository() {
        return Mockito.mock(PaymentOrderRepository.class);
    }

    @Bean
    @Primary
    public PaymentCallbackRecordRepository paymentCallbackRecordRepository() {
        return Mockito.mock(PaymentCallbackRecordRepository.class);
    }

    @Bean
    @Primary
    public PaymentAuditLogRepository paymentAuditLogRepository() {
        return Mockito.mock(PaymentAuditLogRepository.class);
    }
}
