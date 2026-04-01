package com.github.thundax.bacon.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.auth.infra.persistence.mapper.OAuthClientMapper;
import com.github.thundax.bacon.auth.infra.repository.impl.OAuthClientRepositoryImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
        "com.github.thundax.bacon.auth",
        "com.github.thundax.bacon.upms",
        "com.github.thundax.bacon.order",
        "com.github.thundax.bacon.inventory",
        "com.github.thundax.bacon.payment",
        "com.github.thundax.bacon.storage",
        "com.github.thundax.bacon.boot"
})
@MapperScan(basePackages = {
        "com.github.thundax.bacon.auth.infra.persistence.mapper",
        "com.github.thundax.bacon.upms.infra.persistence.mapper"
})
public class BaconMonoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaconMonoApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(OAuthClientRepository.class)
    public OAuthClientRepository oAuthClientRepository(OAuthClientMapper oAuthClientMapper,
                                                       ObjectMapper objectMapper) {
        return new OAuthClientRepositoryImpl(oAuthClientMapper, objectMapper);
    }
}
