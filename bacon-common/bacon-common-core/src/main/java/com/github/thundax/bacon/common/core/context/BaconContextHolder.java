package com.github.thundax.bacon.common.core.context;

public final class BaconContextHolder {

    private static final ThreadLocal<ContextSnapshot> CTX = new ThreadLocal<>();

    private BaconContextHolder() {}

    public static void set(ContextSnapshot context) {
        CTX.set(context);
    }

    public static ContextSnapshot get() {
        ContextSnapshot value = CTX.get();
        return value == null ? ContextSnapshot.systemDefault() : value;
    }

    public static Long currentTenantId() {
        return get().tenantId();
    }

    public static Long currentUserId() {
        return get().userId();
    }

    public static ContextSnapshot capture() {
        return get();
    }

    public static void restore(ContextSnapshot snapshot) {
        set(snapshot == null ? ContextSnapshot.systemDefault() : snapshot);
    }

    public static void clear() {
        CTX.remove();
    }

    public record ContextSnapshot(Long tenantId, Long userId) {

        private static final long SYSTEM_TENANT_ID = 0L;
        private static final long SYSTEM_USER_ID = 0L;

        public static ContextSnapshot systemDefault() {
            return new ContextSnapshot(SYSTEM_TENANT_ID, SYSTEM_USER_ID);
        }
    }
}
