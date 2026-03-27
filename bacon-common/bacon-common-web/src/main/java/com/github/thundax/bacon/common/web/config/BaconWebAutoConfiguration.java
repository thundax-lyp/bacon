package com.github.thundax.bacon.common.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(WebMvcConfiguration.class)
public class BaconWebAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ApiResponseBodyAdvice apiResponseBodyAdvice(ObjectMapper objectMapper) {
        return new ApiResponseBodyAdvice(objectMapper);
    }
}
