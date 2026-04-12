package com.github.thundax.bacon.common.web.config;

import com.github.thundax.bacon.common.web.context.BaconContextFilter;
import com.github.thundax.bacon.common.web.context.BaconContextResolver;
import com.github.thundax.bacon.common.web.context.DefaultBaconContextResolver;
import com.github.thundax.bacon.common.web.resolver.CurrentTenantArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 创建 Servlet Web 场景下的 MVC 配置扩展点，用于承载项目级的拦截器、跨域和消息转换等统一定制。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(InternalApiGuardProperties.class)
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ObjectProvider<InternalApiGuardProperties> internalApiGuardProperties;

    public WebMvcConfiguration(ObjectProvider<InternalApiGuardProperties> internalApiGuardProperties) {
        this.internalApiGuardProperties = internalApiGuardProperties;
    }

    @Bean
    public BaconContextResolver defaultBaconContextResolver() {
        return new DefaultBaconContextResolver();
    }

    @Bean
    public FilterRegistrationBean<BaconContextFilter> baconContextFilterRegistration(BaconContextResolver resolver) {
        FilterRegistrationBean<BaconContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new BaconContextFilter(resolver));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentTenantArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InternalApiGuardProperties properties = internalApiGuardProperties.getIfAvailable();
        if (properties != null && properties.isEnabled()) {
            registry.addInterceptor(new InternalApiGuardInterceptor(properties))
                    .addPathPatterns(properties.getIncludePathPatterns());
        }
    }
}
