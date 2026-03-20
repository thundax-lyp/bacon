package com.github.thundax.bacon.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconApplication.class, args);
    }
}
