package com.github.thundax.bacon.auth.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * OAuth2 授权服务安全配置，仅对 /oauth2/** 请求生效。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthorizationServerConfiguration {

    private static final PathPatternRequestMatcher OAUTH2_PATH_MATCHER =
            PathPatternRequestMatcher.withDefaults().matcher("/oauth2/**");

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(OAUTH2_PATH_MATCHER)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers(OAUTH2_PATH_MATCHER))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
