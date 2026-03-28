package com.github.thundax.bacon.common.core.config;

import com.github.thundax.bacon.common.core.service.VerificationCodeService;
import com.github.thundax.bacon.common.core.service.impl.CacheVerificationCodeServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 创建全局 VerificationCodeService Bean，统一提供基于缓存的验证码能力。
 */
@AutoConfiguration
public class VerificationCodeAutoConfiguration {

    /**
     * 创建默认 VerificationCodeService，供登录、短信、邮件等验证码场景复用。
     *
     * @return 基于缓存的验证码服务
     */
    @Bean
    @ConditionalOnMissingBean(VerificationCodeService.class)
    public VerificationCodeService verificationCodeService() {
        return new CacheVerificationCodeServiceImpl();
    }
}
