package com.github.thundax.bacon.common.security.config;

import com.github.thundax.bacon.common.security.context.CurrentUserResolver;
import com.github.thundax.bacon.common.security.context.CurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.CurrentTenantResolver;
import com.github.thundax.bacon.common.security.context.MonoCurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.MonoCurrentUserProvider;
import com.github.thundax.bacon.common.security.context.SecurityContextCurrentTenantResolver;
import com.github.thundax.bacon.common.security.context.SecurityContextCurrentUserResolver;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentUserProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 统一创建安全基础配置，包含方法安全启用与运行模式相关的当前用户上下文装配。
 */
@AutoConfiguration
@EnableMethodSecurity
public class BaconSecurityConfiguration {

    @Bean
    @Order(2)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .anonymous(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
    public MonoCurrentUserProvider monoCurrentUserProvider() {
        return new MonoCurrentUserProvider(new SecurityContextCurrentUserResolver());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
    @ConditionalOnMissingBean(CurrentTenantProvider.class)
    public MonoCurrentTenantProvider monoCurrentTenantProvider() {
        return new MonoCurrentTenantProvider(new SecurityContextCurrentTenantResolver());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    public SpringContextCurrentUserProvider microCurrentUserProvider(
            ObjectProvider<CurrentUserResolver> currentUserResolver) {
        return new SpringContextCurrentUserProvider(currentUserResolver);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    @ConditionalOnMissingBean(CurrentTenantProvider.class)
    public SpringContextCurrentTenantProvider microCurrentTenantProvider(
            ObjectProvider<CurrentTenantResolver> currentTenantResolver) {
        return new SpringContextCurrentTenantProvider(currentTenantResolver);
    }

    @Bean
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    @ConditionalOnMissingBean(CurrentUserResolver.class)
    public SecurityContextCurrentUserResolver microCurrentUserResolver() {
        return new SecurityContextCurrentUserResolver();
    }

    @Bean
    @ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
    @ConditionalOnMissingBean(CurrentTenantResolver.class)
    public SecurityContextCurrentTenantResolver microCurrentTenantResolver() {
        return new SecurityContextCurrentTenantResolver();
    }
}
