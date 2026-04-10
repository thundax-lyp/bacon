package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

class BaconAsyncContextPropagationTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldPropagateContextThroughAsyncAnnotation() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(BaconAsyncAutoConfiguration.class, AsyncTestConfiguration.class);
            context.refresh();

            BaconContextHolder.set(new BaconContext(1001L, 2001L));
            AsyncTestService service = context.getBean(AsyncTestService.class);

            BaconContext snapshot = service.readContext().get(5, TimeUnit.SECONDS);
            assertThat(snapshot).isEqualTo(new BaconContext(1001L, 2001L));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class AsyncTestConfiguration {

        @Bean
        AsyncTestService asyncTestService() {
            return new AsyncTestService();
        }
    }

    static class AsyncTestService {

        @Async
        public CompletableFuture<BaconContext> readContext() {
            return CompletableFuture.completedFuture(BaconContextHolder.snapshot());
        }
    }
}
