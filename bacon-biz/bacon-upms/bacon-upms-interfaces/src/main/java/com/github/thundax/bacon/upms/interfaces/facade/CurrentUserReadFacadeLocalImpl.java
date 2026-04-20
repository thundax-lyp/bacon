package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.CurrentUserReadFacade;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class CurrentUserReadFacadeLocalImpl implements CurrentUserReadFacade {

    private final UserQueryApplicationService userQueryApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public CurrentUserReadFacadeLocalImpl(
            UserQueryApplicationService userQueryApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userQueryApplicationService = userQueryApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public UserFacadeResponse getCurrentUser() {
        BaconContextHolder.requireTenantId();
        return toFacadeResponse(userQueryApplicationService.getUserById(BaconIdContextHelper.requireUserId()));
    }

    @Override
    public TenantFacadeResponse getCurrentTenant() {
        BaconContextHolder.requireTenantId();
        return toFacadeResponse(userQueryApplicationService.getTenantByTenantId(BaconIdContextHelper.requireTenantId()));
    }

    @Override
    public UserDataScopeFacadeResponse getCurrentDataScope() {
        BaconContextHolder.requireTenantId();
        UserDataScopeDTO dataScope = permissionQueryService.getUserDataScope(UserId.of(BaconIdContextHelper.requireUserId()));
        return UserDataScopeFacadeResponse.from(new UserDataScopeDetailFacadeResponse(
                dataScope.isAllAccess(), dataScope.getScopeTypes(), dataScope.getDepartmentIds()));
    }

    private UserFacadeResponse toFacadeResponse(UserDTO user) {
        return new UserFacadeResponse(
                user.getId(),
                user.getAccount(),
                user.getName(),
                user.getAvatarStoredObjectNo(),
                user.getPhone(),
                user.getDepartmentId(),
                user.getAvatarUrl(),
                user.getStatus());
    }

    private TenantFacadeResponse toFacadeResponse(TenantDTO tenant) {
        return new TenantFacadeResponse(
                tenant.getId(), tenant.getName(), tenant.getCode(), tenant.getStatus(), tenant.getExpiredAt());
    }
}
