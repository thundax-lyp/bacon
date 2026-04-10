package com.github.thundax.bacon.common.core.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AsyncTaskWrapperTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldPropagateContextAcrossThreadPool() throws Exception {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Supplier<BaconContext> supplier =
                    AsyncTaskWrapper.wrap((Supplier<BaconContext>) BaconContextHolder::snapshot);
            CompletableFuture<BaconContext> future = CompletableFuture.supplyAsync(supplier, executor);

            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo(new BaconContext(1001L, 2001L));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldClearThreadLocalAfterWrappedRunnable() throws Exception {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<BaconContext> future = new CompletableFuture<>();
            Runnable task = AsyncTaskWrapper.wrap((Runnable) () -> future.complete(BaconContextHolder.snapshot()));
            executor.execute(task);
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo(new BaconContext(1001L, 2001L));

            CompletableFuture<BaconContext> second = new CompletableFuture<>();
            executor.execute(() -> second.complete(BaconContextHolder.snapshot()));
            assertThat(second.get(5, TimeUnit.SECONDS)).isNull();
        } finally {
            executor.shutdownNow();
        }
    }
}
