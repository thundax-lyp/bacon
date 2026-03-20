package com.github.thundax.bacon.common.cache;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableMethodCache(basePackages = "com.github.thundax.bacon")
public class JetCacheAutoConfiguration {
}
