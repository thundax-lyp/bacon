package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class AuthRepositoryTestConfiguration {

    @Bean
    @Primary
    public AuthSessionRepository authSessionRepository() {
        return Mockito.mock(AuthSessionRepository.class);
    }

    @Bean
    @Primary
    public OAuthClientRepository oAuthClientRepository() {
        return Mockito.mock(OAuthClientRepository.class);
    }

    @Bean
    @Primary
    public OAuthAuthorizationRepository oAuthAuthorizationRepository() {
        return Mockito.mock(OAuthAuthorizationRepository.class);
    }
}
