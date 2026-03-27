package com.github.thundax.bacon.order.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon.order")
public class BaconOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconOrderApplication.class, args);
    }
}
