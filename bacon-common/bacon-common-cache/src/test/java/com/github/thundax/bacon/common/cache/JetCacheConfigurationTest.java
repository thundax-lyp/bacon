package com.github.thundax.bacon.common.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;

class JetCacheConfigurationTest {

    private final JetCacheConfiguration jetCacheConfiguration = new JetCacheConfiguration();

    @Test
    void shouldCustomizeGlobalCacheConfigWithProjectDefaults() {
        BeanPostProcessor beanPostProcessor = jetCacheConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();

        Object processed = beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertThat(processed).isSameAs(globalCacheConfig);
        assertThat(globalCacheConfig.getHiddenPackages()).containsExactly("com.github.thundax.bacon");
        assertThat(globalCacheConfig.isAreaInCacheName()).isTrue();
        assertThat(globalCacheConfig.isPenetrationProtect()).isTrue();
        assertThat(globalCacheConfig.getStatIntervalMinutes()).isEqualTo(15);
    }

    @Test
    void shouldKeepExplicitStatIntervalMinutes() {
        BeanPostProcessor beanPostProcessor = jetCacheConfiguration.jetCacheGlobalConfigBeanPostProcessor();
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setStatIntervalMinutes(5);

        beanPostProcessor.postProcessAfterInitialization(globalCacheConfig, "globalCacheConfig");

        assertThat(globalCacheConfig.getStatIntervalMinutes()).isEqualTo(5);
    }
}
