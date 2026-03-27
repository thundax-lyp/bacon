package com.github.thundax.bacon.common.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.security.context.CurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.CurrentTenantResolver;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.security.context.CurrentUserResolver;
import com.github.thundax.bacon.common.security.context.MonoCurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.MonoCurrentUserProvider;
import com.github.thundax.bacon.common.security.context.SecurityContextCurrentTenantResolver;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentTenantProvider;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.web.SecurityFilterChain;

class BaconSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BaconSecurityConfiguration.class);

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class,
                    UserDetailsServiceAutoConfiguration.class))
            .withUserConfiguration(BaconSecurityConfiguration.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateMethodSecurityExpressionDefaults() {
        contextRunner.run(context -> {
            AnnotationTemplateExpressionDefaults defaults = context.getBean(AnnotationTemplateExpressionDefaults.class);

            assertThat(defaults).isNotNull();
        });
    }

    @Test
    void shouldRegisterDefaultSecurityFilterChainInServletWebApplication() {
        webContextRunner.run(context -> assertThat(context).hasSingleBean(SecurityFilterChain.class));
    }

    @Test
    void shouldWireMonoCurrentUserProviderInMonoMode() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("mono-user", "N/A", "ROLE_USER"));

        contextRunner
                .withPropertyValues("bacon.runtime.mode=mono")
                .run(context -> {
                    CurrentUserProvider currentUserProvider = context.getBean(CurrentUserProvider.class);
                    MonoCurrentUserProvider monoCurrentUserProvider = context.getBean(MonoCurrentUserProvider.class);
                    CurrentTenantProvider currentTenantProvider = context.getBean(CurrentTenantProvider.class);
                    MonoCurrentTenantProvider monoCurrentTenantProvider = context.getBean(MonoCurrentTenantProvider.class);

                    assertThat(currentUserProvider).isInstanceOf(MonoCurrentUserProvider.class);
                    assertThat(monoCurrentUserProvider).isSameAs(currentUserProvider);
                    assertThat(currentUserProvider.currentUserId()).isEqualTo("mono-user");
                    assertThat(currentTenantProvider).isInstanceOf(MonoCurrentTenantProvider.class);
                    assertThat(monoCurrentTenantProvider).isSameAs(currentTenantProvider);
                });
    }

    @Test
    void shouldWireSpringContextCurrentUserProviderInMicroMode() {
        contextRunner
                .withPropertyValues("bacon.runtime.mode=micro")
                .withUserConfiguration(MicroResolverConfiguration.class)
                .run(context -> {
                    CurrentUserProvider currentUserProvider = context.getBean(CurrentUserProvider.class);
                    SpringContextCurrentUserProvider springContextCurrentUserProvider =
                            context.getBean(SpringContextCurrentUserProvider.class);
                    CurrentTenantProvider currentTenantProvider = context.getBean(CurrentTenantProvider.class);
                    SpringContextCurrentTenantProvider springContextCurrentTenantProvider =
                            context.getBean(SpringContextCurrentTenantProvider.class);

                    assertThat(currentUserProvider).isInstanceOf(SpringContextCurrentUserProvider.class);
                    assertThat(springContextCurrentUserProvider).isSameAs(currentUserProvider);
                    assertThat(currentTenantProvider).isInstanceOf(SpringContextCurrentTenantProvider.class);
                    assertThat(springContextCurrentTenantProvider).isSameAs(currentTenantProvider);
                });
    }

    @Test
    void shouldResolveCurrentUserFromSpringContextHolderInMicroProvider() {
        CurrentUserProvider currentUserProvider = new SpringContextCurrentUserProvider(
                new SingleObjectProvider<>(new StubCurrentUserResolver()));
        assertThat(currentUserProvider.currentUserId()).isEqualTo("micro-user");
    }

    @Test
    void shouldResolveCurrentTenantFromSpringContextHolderInMicroProvider() {
        CurrentTenantProvider currentTenantProvider = new SpringContextCurrentTenantProvider(
                new SingleObjectProvider<>(new StubCurrentTenantResolver()));
        assertThat(currentTenantProvider.currentTenantId()).isEqualTo(1001L);
    }

    @Test
    void securityContextTenantResolverShouldReadTenantIdFromDetailsMap() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("mono-user", "N/A", "ROLE_USER");
        authentication.setDetails(java.util.Map.of("tenantId", 1001L));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityContextCurrentTenantResolver resolver = new SecurityContextCurrentTenantResolver();
        assertThat(resolver.currentTenantId()).isEqualTo(1001L);
    }

    @Configuration(proxyBeanMethods = false)
    static class MicroResolverConfiguration {

        @Bean
        CurrentUserResolver currentUserResolver() {
            return () -> "micro-user";
        }
    }

    public static class StubCurrentUserResolver implements CurrentUserResolver {

        @Override
        public String currentUserId() {
            return "micro-user";
        }
    }

    public static class StubCurrentTenantResolver implements CurrentTenantResolver {

        @Override
        public Long currentTenantId() {
            return 1001L;
        }
    }

    static final class SingleObjectProvider<T> implements ObjectProvider<T> {

        private final T value;

        SingleObjectProvider(T value) {
            this.value = value;
        }

        @Override
        public T getObject(Object... args) {
            return value;
        }

        @Override
        public T getIfAvailable() {
            return value;
        }

        @Override
        public T getIfUnique() {
            return value;
        }
    }
}
