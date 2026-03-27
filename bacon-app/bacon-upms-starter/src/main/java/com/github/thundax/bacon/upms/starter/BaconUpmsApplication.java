package com.github.thundax.bacon.upms.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon.upms")
public class BaconUpmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconUpmsApplication.class, args);
    }
}
