package com.github.thundax.bacon.common.core.context;

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

    public record BaconContext(Long tenantId, Long userId) {}
}
