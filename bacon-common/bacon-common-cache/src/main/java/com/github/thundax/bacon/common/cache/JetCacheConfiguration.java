package com.github.thundax.bacon.common.cache;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableMethodCache(basePackages = "com.github.thundax.bacon")
@EnableCreateCacheAnnotation
public class JetCacheConfiguration {

    private static final String BASE_PACKAGE = "com.github.thundax.bacon";
    private static final int DEFAULT_STAT_INTERVAL_MINUTES = 15;

    @Bean
    public BeanPostProcessor jetCacheGlobalConfigBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof GlobalCacheConfig globalCacheConfig) {
                    globalCacheConfig.setHiddenPackages(new String[]{BASE_PACKAGE});
                    globalCacheConfig.setAreaInCacheName(true);
                    globalCacheConfig.setPenetrationProtect(true);
                    if (globalCacheConfig.getStatIntervalMinutes() <= 0) {
                        globalCacheConfig.setStatIntervalMinutes(DEFAULT_STAT_INTERVAL_MINUTES);
                    }
                }
                return bean;
            }
        };
    }
}
