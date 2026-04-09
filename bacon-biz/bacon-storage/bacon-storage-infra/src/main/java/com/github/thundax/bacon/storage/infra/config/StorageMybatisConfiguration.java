package com.github.thundax.bacon.storage.infra.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@MapperScan("com.github.thundax.bacon.storage.infra.persistence.mapper")
public class StorageMybatisConfiguration {}
