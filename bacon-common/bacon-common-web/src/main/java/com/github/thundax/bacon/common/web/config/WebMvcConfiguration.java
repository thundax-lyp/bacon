package com.github.thundax.bacon.common.web.config;

import com.github.thundax.bacon.common.security.context.CurrentTenantProvider;
import com.github.thundax.bacon.common.web.resolver.CurrentTenantArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 创建 Servlet Web 场景下的 MVC 配置扩展点，用于承载项目级的拦截器、跨域和消息转换等统一定制。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ObjectProvider<CurrentTenantProvider> currentTenantProvider;

    public WebMvcConfiguration(ObjectProvider<CurrentTenantProvider> currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        CurrentTenantProvider provider = currentTenantProvider.getIfAvailable();
        if (provider != null) {
            resolvers.add(new CurrentTenantArgumentResolver(provider));
        }
    }
}
