package com.github.thundax.bacon.common.id.context;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.TenantIdCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;

public final class BaconIdContextHelper {

    private BaconIdContextHelper() {}

    public static TenantId currentTenantId() {
        return TenantIdCodec.toDomain(BaconContextHolder.currentTenantId());
    }

    public static UserId currentUserId() {
        return UserIdCodec.toDomain(BaconContextHolder.currentUserId());
    }

    public static TenantId requireTenantId() {
        return TenantIdCodec.toDomain(BaconContextHolder.requireTenantId());
    }

    public static UserId requireUserId() {
        return UserIdCodec.toDomain(BaconContextHolder.requireUserId());
    }
}
