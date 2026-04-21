package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;

public interface PermissionCacheRepository {

    void deleteUserPermission(TenantId tenantId, UserId userId);
}
