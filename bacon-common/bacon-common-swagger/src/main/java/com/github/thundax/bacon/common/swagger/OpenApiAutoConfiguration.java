package com.github.thundax.bacon.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiAutoConfiguration {

    @Bean
    public OpenAPI baconOpenApi() {
        return new OpenAPI().info(new Info().title("Bacon API").version("v1"));
    }
}
