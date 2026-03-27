package com.github.thundax.bacon.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon.register")
public class BaconNacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconNacosApplication.class, args);
    }
}
