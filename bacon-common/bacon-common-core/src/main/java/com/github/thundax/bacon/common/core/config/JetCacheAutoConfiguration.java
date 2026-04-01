package com.github.thundax.bacon.common.core.config;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 创建 JetCache 配置，用于统一启用方法缓存能力并设置全局缓存默认参数。
 */
@AutoConfiguration
@EnableMethodCache(basePackages = "com.github.thundax.bacon")
@EnableCreateCacheAnnotation
public class JetCacheAutoConfiguration {

    private static final String BASE_PACKAGE = "com.github.thundax.bacon";
    private static final int DEFAULT_STAT_INTERVAL_MINUTES = 15;

    /**
     * 创建 BeanPostProcessor，用于在 JetCache 全局配置初始化后补充项目级默认参数。
     */
    public static BeanPostProcessor jetCacheGlobalConfigBeanPostProcessor() {
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
