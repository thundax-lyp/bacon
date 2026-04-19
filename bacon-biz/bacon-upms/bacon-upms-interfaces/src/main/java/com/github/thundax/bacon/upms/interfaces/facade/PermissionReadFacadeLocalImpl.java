package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.api.request.UserPermissionGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserDataScopeDetailFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserMenuTreeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserMenuTreeItemFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserPermissionCodeFacadeResponse;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import java.util.List;
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
    public UserMenuTreeFacadeResponse listMenuTreeByUserId(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserMenuTreeFacadeResponse.from(permissionQueryService.listMenuTreeByUserId(UserId.of(request.getUserId()))
                .stream()
                .map(this::toMenuInfo)
                .toList());
    }

    @Override
    public UserPermissionCodeFacadeResponse findPermissionCodesByUserId(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return UserPermissionCodeFacadeResponse.from(
                permissionQueryService.findPermissionCodesByUserId(UserId.of(request.getUserId())));
    }

    @Override
    public UserDataScopeFacadeResponse getUserDataScope(UserPermissionGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        UserDataScopeDTO dataScope = permissionQueryService.getUserDataScope(UserId.of(request.getUserId()));
        return UserDataScopeFacadeResponse.from(new UserDataScopeDetailFacadeResponse(
                dataScope.isAllAccess(), dataScope.getScopeTypes(), dataScope.getDepartmentIds()));
    }

    private UserMenuTreeItemFacadeResponse toMenuInfo(UserMenuTreeDTO dto) {
        List<UserMenuTreeItemFacadeResponse> children = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(this::toMenuInfo).toList();
        return new UserMenuTreeItemFacadeResponse(
                dto.getId(),
                dto.getName(),
                dto.getMenuType(),
                dto.getParentId(),
                dto.getRoutePath(),
                dto.getComponentName(),
                dto.getIcon(),
                dto.getSort(),
                children);
    }
}
