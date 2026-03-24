package com.github.thundax.bacon.auth.starter.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

class AuthorizationServerConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SecurityAutoConfiguration.class,
                    SecurityFilterAutoConfiguration.class,
                    UserDetailsServiceAutoConfiguration.class
            ))
            .withUserConfiguration(AuthorizationServerConfiguration.class);

    private final ApplicationContextRunner nonWebContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AuthorizationServerConfiguration.class);

    @Test
    void shouldRegisterSecurityFilterChainInServletWebApplication() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthorizationServerConfiguration.class);
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
        });
    }

    @Test
    void shouldNotRegisterSecurityFilterChainInNonWebApplication() {
        nonWebContextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(AuthorizationServerConfiguration.class);
            assertThat(context).doesNotHaveBean(SecurityFilterChain.class);
        });
    }
}
