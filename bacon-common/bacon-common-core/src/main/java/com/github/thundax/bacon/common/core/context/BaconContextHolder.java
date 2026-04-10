package com.github.thundax.bacon.common.core.context;

import java.util.function.Supplier;

public final class BaconContextHolder {

    private static final ThreadLocal<BaconContext> HOLDER = new ThreadLocal<>();

    private BaconContextHolder() {}

    public static void set(BaconContext context) {
        if (context == null) {
            clear();
            return;
        }
        HOLDER.set(context);
    }

    public static BaconContext get() {
        return HOLDER.get();
    }

    public static BaconContext snapshot() {
        BaconContext context = HOLDER.get();
        return context == null ? null : new BaconContext(context.tenantId(), context.userId());
    }

    public static void restore(BaconContext context) {
        set(context);
    }

    public static Long currentTenantId() {
        BaconContext context = HOLDER.get();
        return context == null ? null : context.tenantId();
    }

    public static Long currentUserId() {
        BaconContext context = HOLDER.get();
        return context == null ? null : context.userId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static void runWithTenantId(Long tenantId, Runnable action) {
        callWithTenantId(tenantId, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T callWithTenantId(Long tenantId, Supplier<T> supplier) {
        BaconContext previous = snapshot();
        Long userId = previous == null ? null : previous.userId();
        try {
            set(new BaconContext(tenantId, userId));
            return supplier.get();
        } finally {
            restore(previous);
        }
    }

    public record BaconContext(Long tenantId, Long userId) {}
}
