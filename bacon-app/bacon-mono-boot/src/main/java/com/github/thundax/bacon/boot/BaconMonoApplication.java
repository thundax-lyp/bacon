package com.github.thundax.bacon.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.github.thundax.bacon.auth",
        "com.github.thundax.bacon.upms",
        "com.github.thundax.bacon.order",
        "com.github.thundax.bacon.inventory",
        "com.github.thundax.bacon.payment",
        "com.github.thundax.bacon.storage",
        "com.github.thundax.bacon.boot"
})
@MapperScan(basePackages = {
        "com.github.thundax.bacon.upms.infra.persistence.mapper"
})
public class BaconMonoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconMonoApplication.class, args);
    }
}
