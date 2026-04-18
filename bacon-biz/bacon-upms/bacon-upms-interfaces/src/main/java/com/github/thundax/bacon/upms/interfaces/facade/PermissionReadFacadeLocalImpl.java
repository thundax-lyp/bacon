package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.api.request.UserPermissionGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserMenuTreeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserPermissionCodeFacadeResponse;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PermissionReadFacadeLocalImpl implements PermissionReadFacade {

    private final PermissionQueryApplicationService permissionQueryService;

    public PermissionReadFacadeLocalImpl(PermissionQueryApplicationService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public UserMenuTreeFacadeResponse listUserMenuTree(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserMenuTreeFacadeResponse.from(permissionQueryService.listUserMenuTree(UserId.of(request.getUserId())));
    }

    @Override
    public UserPermissionCodeFacadeResponse findUserPermissionCodes(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserPermissionCodeFacadeResponse.from(
                permissionQueryService.findUserPermissionCodes(UserId.of(request.getUserId())));
    }

    @Override
    public UserDataScopeFacadeResponse getUserDataScope(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserDataScopeFacadeResponse.from(permissionQueryService.getUserDataScope(UserId.of(request.getUserId())));
    }
}
