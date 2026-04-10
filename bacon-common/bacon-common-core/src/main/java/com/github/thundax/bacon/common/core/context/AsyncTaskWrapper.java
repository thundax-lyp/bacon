package com.github.thundax.bacon.common.core.context;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class AsyncTaskWrapper {

    private AsyncTaskWrapper() {}

    public static Runnable wrap(Runnable task) {
        BaconContextHolder.BaconContext snapshot = BaconContextHolder.snapshot();
        return () -> {
            try {
                BaconContextHolder.restore(snapshot);
                task.run();
            } finally {
                BaconContextHolder.clear();
            }
        };
    }

    public static <V> Callable<V> wrap(Callable<V> task) {
        BaconContextHolder.BaconContext snapshot = BaconContextHolder.snapshot();
        return () -> {
            try {
                BaconContextHolder.restore(snapshot);
                return task.call();
            } finally {
                BaconContextHolder.clear();
            }
        };
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        BaconContextHolder.BaconContext snapshot = BaconContextHolder.snapshot();
        return () -> {
            try {
                BaconContextHolder.restore(snapshot);
                return supplier.get();
            } finally {
                BaconContextHolder.clear();
            }
        };
    }
}
