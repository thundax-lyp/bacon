package com.github.thundax.bacon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconGatewayApplication.class, args);
    }
}
