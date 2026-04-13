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

    public static Long requireTenantId() {
        Long tenantId = currentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("tenantId must not be null");
        }
        return tenantId;
    }

    public static Long requireUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new IllegalStateException("userId must not be null");
        }
        return userId;
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
        BaconContext current = snapshot();
        Long userId = current == null ? null : current.userId();
        return callWithContext(new BaconContext(tenantId, userId), supplier);
    }

    public static void runWithCurrentContext(Runnable action) {
        callWithCurrentContext(() -> {
            action.run();
            return null;
        });
    }

    public static <T> T callWithCurrentContext(Supplier<T> supplier) {
        return callWithContext(snapshot(), supplier);
    }

    private static <T> T callWithContext(BaconContext context, Supplier<T> supplier) {
        BaconContext previous = snapshot();
        try {
            restore(context);
            return supplier.get();
        } finally {
            restore(previous);
        }
    }

    public record BaconContext(Long tenantId, Long userId) {}
}
