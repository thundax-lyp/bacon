package com.github.thundax.bacon.inventory.infra.config;

import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class InventoryRepositoryModeValidationConfiguration {

    private static final String MODE_KEY = "bacon.inventory.repository.mode";

    @Bean
    public SmartInitializingSingleton inventoryRepositoryModeValidator(
            Environment environment,
            ObjectProvider<InventoryStockRepository> inventoryStockRepositoryProvider) {
        return () -> {
            String mode = environment.getProperty(MODE_KEY, "strict").trim().toLowerCase(Locale.ROOT);
            if (!"strict".equals(mode) && !"memory".equals(mode)) {
                throw new IllegalStateException("Invalid inventory repository mode: " + mode
                        + ", only 'strict' and 'memory' are allowed");
            }
            if ("strict".equals(mode) && inventoryStockRepositoryProvider.getIfAvailable() == null) {
                throw new IllegalStateException("Inventory repository mode is strict, but no persistent repository bean found. "
                        + "Please check datasource/mybatis configuration or switch mode explicitly.");
            }
            if ("memory".equals(mode)) {
                log.warn("Inventory repository mode=memory, data is in-memory only and not persisted");
                return;
            }
            log.info("Inventory repository mode=strict, persistent repository is enabled");
        };
    }
}
