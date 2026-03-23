package com.github.thundax.bacon.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.github.thundax.bacon.common.mybatis.fill.MybatisPlusMetaObjectHandler;
import com.github.thundax.bacon.common.mybatis.handler.IntegerArrayTypeHandler;
import com.github.thundax.bacon.common.mybatis.handler.LongArrayTypeHandler;
import com.github.thundax.bacon.common.mybatis.handler.StringArrayTypeHandler;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.security.context.DefaultCurrentUserProvider;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class MybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock mybatisClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserProvider currentUserProvider() {
        return new DefaultCurrentUserProvider();
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler mybatisPlusMetaObjectHandler(Clock mybatisClock,
                                                          CurrentUserProvider currentUserProvider) {
        return new MybatisPlusMetaObjectHandler(mybatisClock, currentUserProvider);
    }

    @Bean
    @ConditionalOnMissingBean(name = "mybatisPlusTypeHandlerCustomizer")
    public ConfigurationCustomizer mybatisPlusTypeHandlerCustomizer() {
        return configuration -> registerDefaultTypeHandlers(configuration.getTypeHandlerRegistry());
    }

    private void registerDefaultTypeHandlers(TypeHandlerRegistry registry) {
        registry.register(String[].class, StringArrayTypeHandler.class);
        registry.register(Long[].class, LongArrayTypeHandler.class);
        registry.register(Integer[].class, IntegerArrayTypeHandler.class);
    }
}
