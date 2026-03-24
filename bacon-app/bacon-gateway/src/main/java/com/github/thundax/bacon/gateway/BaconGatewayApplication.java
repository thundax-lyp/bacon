package com.github.thundax.bacon.gateway;

import com.github.thundax.bacon.common.security.config.BaconSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(BaconSecurityConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconGatewayApplication.class, args);
    }
}
