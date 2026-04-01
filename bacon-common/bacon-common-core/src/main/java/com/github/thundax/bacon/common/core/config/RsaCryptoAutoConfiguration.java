package com.github.thundax.bacon.common.core.config;

import com.github.thundax.bacon.common.core.service.RsaCryptoService;
import com.github.thundax.bacon.common.core.service.impl.DefaultRsaCryptoServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RsaCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RsaCryptoService.class)
    public RsaCryptoService rsaCryptoService() {
        return new DefaultRsaCryptoServiceImpl();
    }
}
