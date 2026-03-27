package com.github.thundax.bacon.payment.infra.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(SqlSessionFactory.class)
@MapperScan("com.github.thundax.bacon.payment.infra.persistence.mapper")
public class PaymentMybatisConfiguration {
}
