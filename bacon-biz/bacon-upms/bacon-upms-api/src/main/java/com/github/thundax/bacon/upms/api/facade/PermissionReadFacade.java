package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

public interface PermissionReadFacade {

    List<UserMenuTreeDTO> getUserMenuTree(@NonNull TenantId tenantId, @NonNull UserId userId);

    Set<String> getUserPermissionCodes(@NonNull TenantId tenantId, @NonNull UserId userId);

    UserDataScopeDTO getUserDataScope(@NonNull TenantId tenantId, @NonNull UserId userId);
}
