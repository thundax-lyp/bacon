package com.github.thundax.bacon.payment.infra.config;

import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class PaymentRepositoryModeValidationConfiguration {

    private static final String MODE_KEY = "bacon.payment.repository.mode";

    @Bean
    public SmartInitializingSingleton paymentRepositoryModeValidator(
            Environment environment,
            ObjectProvider<PaymentOrderRepository> paymentOrderRepositoryProvider) {
        return () -> {
            String mode = environment.getProperty(MODE_KEY, "strict").trim().toLowerCase(Locale.ROOT);
            if (!"strict".equals(mode) && !"memory".equals(mode)) {
                throw new IllegalStateException("Invalid payment repository mode: " + mode
                        + ", only 'strict' and 'memory' are allowed");
            }
            if ("strict".equals(mode) && paymentOrderRepositoryProvider.getIfAvailable() == null) {
                throw new IllegalStateException("Payment repository mode is strict, but no persistent repository bean found. "
                        + "Please check datasource/mybatis configuration or switch mode explicitly.");
            }
            if ("memory".equals(mode)) {
                log.warn("Payment repository mode=memory, data is in-memory only and not persisted");
                return;
            }
            log.info("Payment repository mode=strict, persistent repository is enabled");
        };
    }
}
