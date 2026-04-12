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
        return TenantIdMapper.toDomain(BaconContextHolder.requireTenantId());
    }

    public static UserId requireUserId() {
        return UserIdMapper.toDomain(BaconContextHolder.requireUserId());
    }
}
