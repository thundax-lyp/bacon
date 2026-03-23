package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.common.security.config.BaconMybatisSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableFeignClients(basePackages = "com.github.thundax.bacon")
@Import(BaconMybatisSecurityConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconApplication.class, args);
    }
}
