package com.github.thundax.bacon.common.id.context;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
import com.github.thundax.bacon.common.id.mapper.UserIdMapper;

public final class BaconIdContextHelper {

    private BaconIdContextHelper() {}

    public static TenantId currentTenantId() {
        return TenantIdMapper.toDomain(BaconContextHolder.currentTenantId());
    }

    public static UserId currentUserId() {
        return UserIdMapper.toDomain(BaconContextHolder.currentUserId());
    }

    public static TenantId requireTenantId() {
        TenantId tenantId = currentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("tenantId must not be null");
        }
        return tenantId;
    }

    public static UserId requireUserId() {
        UserId userId = currentUserId();
        if (userId == null) {
            throw new IllegalStateException("userId must not be null");
        }
        return userId;
    }
}
