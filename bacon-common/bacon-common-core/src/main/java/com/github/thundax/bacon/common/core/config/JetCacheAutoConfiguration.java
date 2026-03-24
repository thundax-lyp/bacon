package com.github.thundax.bacon.common.core.config;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;

/**
 * 创建 JetCache 配置，用于统一启用方法缓存能力并设置全局缓存默认参数。
 */
@AutoConfiguration
@EnableMethodCache(basePackages = "com.github.thundax.bacon")
@EnableCreateCacheAnnotation
public class JetCacheAutoConfiguration {

    private static final String BASE_PACKAGE = "com.github.thundax.bacon";
    private static final String DEFAULT_AREA = "default";
    private static final int DEFAULT_STAT_INTERVAL_MINUTES = 15;
    private static final int DEFAULT_LOCAL_LIMIT = 10_000;
    private static final int DEFAULT_EXPIRE_SECONDS = 300;

    /**
     * 创建兜底 GlobalCacheConfig，在未显式声明 JetCache builder 时提供默认内存缓存能力。
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalCacheConfig globalCacheConfig() {
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setHiddenPackages(new String[]{BASE_PACKAGE});
        globalCacheConfig.setAreaInCacheName(true);
        globalCacheConfig.setPenetrationProtect(true);
        globalCacheConfig.setEnableMethodCache(true);
        globalCacheConfig.setStatIntervalMinutes(DEFAULT_STAT_INTERVAL_MINUTES);
        globalCacheConfig.setLocalCacheBuilders(Map.of(
                DEFAULT_AREA,
                LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                        .limit(DEFAULT_LOCAL_LIMIT)
                        .expireAfterWrite(DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS)
        ));
        globalCacheConfig.setRemoteCacheBuilders(Map.of(
                DEFAULT_AREA,
                LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                        .limit(DEFAULT_LOCAL_LIMIT)
                        .expireAfterWrite(DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS)
        ));
        return globalCacheConfig;
    }

    /**
     * 创建 BeanPostProcessor，用于在 JetCache 全局配置初始化后补充项目级默认参数。
     */
    @Bean
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
