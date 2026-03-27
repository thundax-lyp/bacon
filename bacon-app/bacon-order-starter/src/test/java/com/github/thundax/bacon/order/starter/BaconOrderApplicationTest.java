package com.github.thundax.bacon.order.starter;

import com.github.thundax.bacon.common.test.BaconSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BaconOrderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
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
                "bacon.order.repository.mode=memory",
                "bacon.remote.order-base-url=http://127.0.0.1:18084"
        })
class BaconOrderApplicationTest extends BaconSpringBootTest {

    @Test
    void contextLoads() {
    }
}
