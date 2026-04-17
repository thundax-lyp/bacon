package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractConcurrencyArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = ConcurrencyArchitectureRuleSupport.importProjectClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    void shouldNotCreateRawThreads() {
        ConcurrencyArchitectureRuleSupport.shouldNotCreateRawThreads(basePackage())
                .check(classes());
    }

    @Test
    void shouldNotUseExecutorsFactory() {
        ConcurrencyArchitectureRuleSupport.shouldNotUseExecutorsFactory(basePackage())
                .check(classes());
    }

    @Test
    void shouldNotUseCompletableFutureAsyncWithoutExecutor() {
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(basePackage())
                .check(classes());
    }
}
