package com.github.thundax.bacon.common.seata.config;

import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass({GlobalTransactionScanner.class, DataSourceProxy.class})
@ConditionalOnProperty(value = "bacon.seata.enabled", havingValue = "true")
@EnableConfigurationProperties(BaconSeataProperties.class)
public class BaconSeataAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSourceProxy.class)
    public DataSourceProxy baconDataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(GlobalTransactionScanner.class)
    public GlobalTransactionScanner baconGlobalTransactionScanner(
            Environment environment, BaconSeataProperties properties) {
        String applicationId = environment.getProperty("spring.application.name");
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalStateException("spring.application.name must be set when Seata is enabled");
        }
        return new GlobalTransactionScanner(applicationId, properties.getTxServiceGroup());
    }
}
