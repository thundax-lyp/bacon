package com.github.thundax.bacon.common.security.config;

import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.security.context.CurrentUserResolver;
import com.github.thundax.bacon.common.security.context.MonoCurrentUserProvider;
import com.github.thundax.bacon.common.security.context.SecurityContextCurrentUserResolver;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentUserProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
public class BaconMybatisSecurityConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
    public MonoCurrentUserProvider monoCurrentUserProvider() {
        return new MonoCurrentUserProvider(new SecurityContextCurrentUserResolver());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    public SpringContextCurrentUserProvider microCurrentUserProvider() {
        return new SpringContextCurrentUserProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    @ConditionalOnMissingBean(CurrentUserResolver.class)
    public SecurityContextCurrentUserResolver microCurrentUserResolver() {
        return new SecurityContextCurrentUserResolver();
    }
}
