package com.github.thundax.bacon.inventory.starter;

import com.github.thundax.bacon.common.test.BaconSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BaconInventoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.profiles.active=test",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.boot.admin.client.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                        + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
                "bacon.inventory.repository.mode=memory",
                "bacon.remote.inventory-base-url=http://127.0.0.1:18085"
        })
class BaconInventoryApplicationTest extends BaconSpringBootTest {

    @Test
    void contextLoads() {
    }
}
