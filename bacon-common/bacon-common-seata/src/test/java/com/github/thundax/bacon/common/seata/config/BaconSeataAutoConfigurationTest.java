package com.github.thundax.bacon.common.seata.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.seata.spring.annotation.GlobalTransactionScanner;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BaconSeataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BaconSeataAutoConfiguration.class));

    @Test
    void shouldNotRegisterSeataBeansWhenDisabled() {
        contextRunner
                .withBean(DataSource.class, BaconSeataAutoConfigurationTest::stubDataSource)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(GlobalTransactionScanner.class);
                });
    }

    @Test
    void shouldFailWhenApplicationNameMissing() {
        contextRunner
                .withPropertyValues("bacon.seata.enabled=true")
                .withBean(DataSource.class, BaconSeataAutoConfigurationTest::stubDataSource)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("spring.application.name must be set when Seata is enabled");
                });
    }

    private static DataSource stubDataSource() {
        return (DataSource) Proxy.newProxyInstance(
                BaconSeataAutoConfigurationTest.class.getClassLoader(),
                new Class<?>[] {DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        return Proxy.newProxyInstance(
                                BaconSeataAutoConfigurationTest.class.getClassLoader(),
                                new Class<?>[] {Connection.class},
                                (connectionProxy, connectionMethod, connectionArgs) -> {
                                    if ("getMetaData".equals(connectionMethod.getName())) {
                                        return Proxy.newProxyInstance(
                                                BaconSeataAutoConfigurationTest.class.getClassLoader(),
                                                new Class<?>[] {DatabaseMetaData.class},
                                                (metadataProxy, metadataMethod, metadataArgs) -> {
                                                    if ("getURL".equals(metadataMethod.getName())) {
                                                        return "jdbc:mysql://localhost:3306/test";
                                                    }
                                                    Class<?> metadataReturnType = metadataMethod.getReturnType();
                                                    if (metadataReturnType.equals(boolean.class)) {
                                                        return false;
                                                    }
                                                    if (metadataReturnType.equals(int.class)) {
                                                        return 0;
                                                    }
                                                    if (metadataReturnType.equals(long.class)) {
                                                        return 0L;
                                                    }
                                                    return null;
                                                });
                                    }
                                    if ("isClosed".equals(connectionMethod.getName())) {
                                        return false;
                                    }
                                    Class<?> connectionReturnType = connectionMethod.getReturnType();
                                    if (connectionReturnType.equals(boolean.class)) {
                                        return false;
                                    }
                                    if (connectionReturnType.equals(int.class)) {
                                        return 0;
                                    }
                                    if (connectionReturnType.equals(long.class)) {
                                        return 0L;
                                    }
                                    return null;
                                });
                    }
                    if ("unwrap".equals(method.getName())) {
                        return null;
                    }
                    if ("isWrapperFor".equals(method.getName())) {
                        return false;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType.equals(boolean.class)) {
                        return false;
                    }
                    if (returnType.equals(int.class)) {
                        return 0;
                    }
                    if (returnType.equals(long.class)) {
                        return 0L;
                    }
                    return null;
                });
    }

}
