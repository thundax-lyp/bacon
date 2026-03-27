package com.github.thundax.bacon.auth.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon.auth")
public class BaconAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconAuthApplication.class, args);
    }
}
