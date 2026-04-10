package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("不得直接 new Thread")
    void shouldNotCreateRawThreads() {
        ConcurrencyArchitectureRuleSupport.shouldNotCreateRawThreads(basePackage()).check(classes());
    }

    @Test
    @DisplayName("不得直接使用 Executors 工厂")
    void shouldNotUseExecutorsFactory() {
        ConcurrencyArchitectureRuleSupport.shouldNotUseExecutorsFactory(basePackage()).check(classes());
    }

    @Test
    @DisplayName("CompletableFuture 异步工厂必须显式传入 Executor")
    void shouldNotUseCompletableFutureAsyncWithoutExecutor() {
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(basePackage())
                .check(classes());
    }
}
