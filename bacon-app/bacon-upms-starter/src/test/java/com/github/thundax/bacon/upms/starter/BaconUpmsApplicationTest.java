package com.github.thundax.bacon.upms.starter;

import com.github.thundax.bacon.common.test.BaconSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = BaconUpmsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.boot.admin.client.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                        + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
                "bacon.remote.auth-base-url=http://127.0.0.1:18083"
        })
@Import(UpmsRepositoryTestConfiguration.class)
class BaconUpmsApplicationTest extends BaconSpringBootTest {

    @Test
    void contextLoads() {
    }
}
