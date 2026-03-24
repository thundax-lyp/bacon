package com.github.thundax.bacon.common.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 创建 Servlet Web 场景下的 MVC 配置扩展点，用于承载项目级的拦截器、跨域和消息转换等统一定制。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfiguration implements WebMvcConfigurer {
}
