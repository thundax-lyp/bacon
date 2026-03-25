package com.github.thundax.bacon.inventory.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@MapperScan("com.github.thundax.bacon.inventory.infra.persistence.mapper")
public class InventoryMybatisConfiguration {
}
