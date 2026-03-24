package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class WebMvcConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(WebMvcConfiguration.class);

    private final ApplicationContextRunner nonWebContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(WebMvcConfiguration.class);

    @Test
    void shouldRegisterWebMvcConfigurerInServletWebApplication() {
        webContextRunner.run(context -> {
            assertThat(context).hasSingleBean(WebMvcConfiguration.class);
            assertThat(context).hasSingleBean(WebMvcConfigurer.class);
        });
    }

    @Test
    void shouldNotRegisterWebMvcConfigurerInNonWebApplication() {
        nonWebContextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(WebMvcConfiguration.class);
            assertThat(context).doesNotHaveBean(WebMvcConfigurer.class);
        });
    }
}
