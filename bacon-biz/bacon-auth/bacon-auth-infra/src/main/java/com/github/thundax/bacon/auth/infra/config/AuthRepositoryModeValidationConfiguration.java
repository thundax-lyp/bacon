package com.github.thundax.bacon.auth.infra.config;

import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthRepositoryModeValidationConfiguration {

    private static final String MODE_KEY = "bacon.auth.repository.mode";

    @Bean
    public SmartInitializingSingleton authRepositoryModeValidator(
            Environment environment,
            ObjectProvider<AuthSessionRepository> authSessionRepositoryProvider,
            ObjectProvider<OAuthAuthorizationRepository> oAuthAuthorizationRepositoryProvider,
            ObjectProvider<OAuthClientRepository> oAuthClientRepositoryProvider) {
        return () -> {
            String mode = environment.getProperty(MODE_KEY, "strict").trim().toLowerCase(Locale.ROOT);
            if (!"strict".equals(mode) && !"memory".equals(mode)) {
                throw new IllegalStateException("Invalid auth repository mode: " + mode
                        + ", only 'strict' and 'memory' are allowed");
            }
            if ("memory".equals(mode) && !isTestProfile(environment)) {
                throw new IllegalStateException("Auth repository mode=memory is test-only. "
                        + "Please activate test profile or switch mode to strict.");
            }
            if ("strict".equals(mode)
                    && (authSessionRepositoryProvider.getIfAvailable() == null
                    || oAuthAuthorizationRepositoryProvider.getIfAvailable() == null
                    || oAuthClientRepositoryProvider.getIfAvailable() == null)) {
                throw new IllegalStateException("Auth repository mode is strict, but persistent repository beans are incomplete. "
                        + "Please check datasource/redis configuration.");
            }
            if ("memory".equals(mode)) {
                log.warn("Auth repository mode=memory, auth data is in-memory only and not persisted");
                return;
            }
            log.info("Auth repository mode=strict, persistent repositories are enabled");
        };
    }

    private boolean isTestProfile(Environment environment) {
        Set<String> activeProfiles = Set.copyOf(Arrays.asList(environment.getActiveProfiles()));
        return activeProfiles.contains("test");
    }
}
