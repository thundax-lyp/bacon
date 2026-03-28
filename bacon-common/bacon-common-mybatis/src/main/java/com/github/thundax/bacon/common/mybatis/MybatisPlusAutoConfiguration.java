package com.github.thundax.bacon.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.github.thundax.bacon.common.mybatis.fill.MybatisPlusMetaObjectHandler;
import com.github.thundax.bacon.common.mybatis.handler.IntegerArrayTypeHandler;
import com.github.thundax.bacon.common.mybatis.handler.LongArrayTypeHandler;
import com.github.thundax.bacon.common.mybatis.handler.StringArrayTypeHandler;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration
public class MybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock mybatisClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserProvider mybatisFallbackCurrentUserProvider(ObjectProvider<CurrentUserProvider> currentUserProvider) {
        return currentUserProvider.getIfAvailable(() -> () -> "system");
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
