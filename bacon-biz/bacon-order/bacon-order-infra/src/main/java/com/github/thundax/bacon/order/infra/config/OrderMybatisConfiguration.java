package com.github.thundax.bacon.order.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
@MapperScan("com.github.thundax.bacon.order.infra.persistence.mapper")
public class OrderMybatisConfiguration {
}
