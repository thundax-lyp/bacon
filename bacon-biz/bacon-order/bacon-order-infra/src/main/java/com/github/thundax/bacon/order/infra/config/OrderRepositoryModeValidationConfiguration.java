package com.github.thundax.bacon.order.infra.config;

import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class OrderRepositoryModeValidationConfiguration {

    private static final String MODE_KEY = "bacon.order.repository.mode";

    @Bean
    public SmartInitializingSingleton orderRepositoryModeValidator(
            Environment environment,
            ObjectProvider<OrderRepository> orderRepositoryProvider) {
        return () -> {
            String mode = environment.getProperty(MODE_KEY, "strict").trim().toLowerCase(Locale.ROOT);
            if (!"strict".equals(mode) && !"memory".equals(mode)) {
                throw new IllegalStateException("Invalid order repository mode: " + mode
                        + ", only 'strict' and 'memory' are allowed");
            }
            if ("memory".equals(mode) && !isTestProfile(environment)) {
                throw new IllegalStateException("Order repository mode=memory is test-only. "
                        + "Please activate test profile or switch mode to strict.");
            }
            if ("strict".equals(mode) && orderRepositoryProvider.getIfAvailable() == null) {
                throw new IllegalStateException("Order repository mode is strict, but no persistent repository bean found. "
                        + "Please check datasource/mybatis configuration.");
            }
            if ("memory".equals(mode)) {
                log.warn("Order repository mode=memory, data is in-memory only and not persisted");
                return;
            }
            log.info("Order repository mode=strict, persistent repository is enabled");
        };
    }

    private boolean isTestProfile(Environment environment) {
        Set<String> activeProfiles = Set.copyOf(Arrays.asList(environment.getActiveProfiles()));
        return activeProfiles.contains("test");
    }
}
