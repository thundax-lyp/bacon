package com.github.thundax.bacon.common.core.config;

import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import com.github.thundax.bacon.common.core.service.impl.CacheVerificationCodeService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建全局 VerificationCodeService Bean，统一提供基于缓存的验证码能力。
 */
@Configuration(proxyBeanMethods = false)
public class VerificationCodeConfiguration {

    /**
     * 创建默认 VerificationCodeService，供登录、短信、邮件等验证码场景复用。
     *
     * @return 基于缓存的验证码服务
     */
    @Bean
    @ConditionalOnMissingBean(VerificationCodeService.class)
    public VerificationCodeService verificationCodeService() {
        return new CacheVerificationCodeService();
    }
}
