package com.github.thundax.bacon.payment.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
@MapperScan("com.github.thundax.bacon.payment.infra.persistence.mapper")
public class PaymentMybatisConfiguration {
}
