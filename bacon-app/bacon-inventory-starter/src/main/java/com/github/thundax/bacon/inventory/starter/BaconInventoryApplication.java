package com.github.thundax.bacon.inventory.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconInventoryApplication.class, args);
    }
}
