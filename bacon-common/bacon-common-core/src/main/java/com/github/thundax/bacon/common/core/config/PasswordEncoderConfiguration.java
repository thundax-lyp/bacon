package com.github.thundax.bacon.common.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 创建全局 PasswordEncoder Bean，统一提供 BCrypt 密码哈希与验密能力。
 */
@Configuration(proxyBeanMethods = false)
public class PasswordEncoderConfiguration {

    private static final int BCRYPT_STRENGTH = 12;

    /**
     * 创建默认 PasswordEncoder，供用户密码存储和登录验密复用。
     *
     * @return BCrypt PasswordEncoder
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
