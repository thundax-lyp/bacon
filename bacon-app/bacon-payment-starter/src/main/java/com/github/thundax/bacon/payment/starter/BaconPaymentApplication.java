package com.github.thundax.bacon.payment.starter;

import com.github.thundax.bacon.common.security.config.BaconMybatisSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(BaconMybatisSecurityConfiguration.class)
@SpringBootApplication(scanBasePackages = "com.github.thundax.bacon")
public class BaconPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconPaymentApplication.class, args);
    }
}
