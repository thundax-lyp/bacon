package com.github.thundax.bacon.payment.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconPaymentApplication.class, args);
    }
}
