package com.github.thundax.bacon.common.security.config;

import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.security.context.CurrentUserResolver;
import com.github.thundax.bacon.common.security.context.MonoCurrentUserProvider;
import com.github.thundax.bacon.common.security.context.SpringBootContext;
import com.github.thundax.bacon.common.security.context.SpringContextCurrentUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class BaconMybatisSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BaconMybatisSecurityConfiguration.class, SpringBootContextConfiguration.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldWireMonoCurrentUserProviderInMonoMode() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("mono-user", "N/A", "ROLE_USER"));

        contextRunner
                .withPropertyValues("bacon.runtime.mode=mono")
                .run(context -> {
                    CurrentUserProvider currentUserProvider = context.getBean(CurrentUserProvider.class);

                    assertThat(currentUserProvider).isInstanceOf(MonoCurrentUserProvider.class);
                    assertThat(currentUserProvider.currentUserId()).isEqualTo("mono-user");
                });
    }

    @Test
    void shouldWireSpringContextCurrentUserProviderInMicroMode() {
        contextRunner
                .withPropertyValues("bacon.runtime.mode=micro")
                .withUserConfiguration(MicroResolverConfiguration.class)
                .run(context -> {
                    CurrentUserProvider currentUserProvider = context.getBean(CurrentUserProvider.class);

                    assertThat(currentUserProvider).isInstanceOf(SpringContextCurrentUserProvider.class);
                });
    }

    @Test
    void shouldResolveCurrentUserFromSpringBootContextInMicroProvider() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("currentUserResolver", StubCurrentUserResolver.class);
        applicationContext.refresh();

        SpringBootContext springBootContext = new SpringBootContext();
        springBootContext.setApplicationContext(applicationContext);

        CurrentUserProvider currentUserProvider = new SpringContextCurrentUserProvider();
        assertThat(currentUserProvider.currentUserId()).isEqualTo("micro-user");
    }

    @Configuration(proxyBeanMethods = false)
    static class MicroResolverConfiguration {

        @Bean
        CurrentUserResolver currentUserResolver() {
            return () -> "micro-user";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class SpringBootContextConfiguration {

        @Bean
        SpringBootContext springBootContext() {
            return new SpringBootContext();
        }
    }

    public static class StubCurrentUserResolver implements CurrentUserResolver {

        @Override
        public String currentUserId() {
            return "micro-user";
        }
    }
}
