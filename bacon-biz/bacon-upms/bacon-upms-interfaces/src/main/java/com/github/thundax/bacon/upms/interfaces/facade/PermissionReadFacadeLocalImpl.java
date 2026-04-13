package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PermissionReadFacadeLocalImpl implements PermissionReadFacade {

    private final PermissionQueryApplicationService permissionQueryService;

    public PermissionReadFacadeLocalImpl(PermissionQueryApplicationService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(@NonNull UserId userId) {
        BaconContextHolder.requireTenantId();
        return permissionQueryService.getUserMenuTree(userId);
    }

    @Override
    public Set<String> getUserPermissionCodes(@NonNull UserId userId) {
        BaconContextHolder.requireTenantId();
        return permissionQueryService.getUserPermissionCodes(userId);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(@NonNull UserId userId) {
        BaconContextHolder.requireTenantId();
        return permissionQueryService.getUserDataScope(userId);
    }
}
