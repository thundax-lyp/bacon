package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractPathArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = PathArchitectureRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    void shouldEnforcePathControllerPrefix() {
        PathArchitectureRuleSupport.controllerRequestMappingShouldUseDomainPrefix(basePackage()).check(classes());
    }

    @Test
    void shouldEnforcePathProviderPrefix() {
        PathArchitectureRuleSupport.providerControllerRequestMappingShouldUseDomainPrefix(basePackage())
                .check(classes());
    }
}
