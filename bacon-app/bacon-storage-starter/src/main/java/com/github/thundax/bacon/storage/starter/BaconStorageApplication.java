package com.github.thundax.bacon.storage.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon.storage")
public class BaconStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconStorageApplication.class, args);
    }
}
