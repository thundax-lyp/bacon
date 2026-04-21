package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.upms.api.facade.CurrentUserReadFacade;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.interfaces.assembler.UserInterfaceAssembler;
import com.github.thundax.bacon.upms.interfaces.assembler.TenantInterfaceAssembler;
import com.github.thundax.bacon.upms.application.query.DepartmentQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import com.github.thundax.bacon.upms.application.query.UserQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class CurrentUserReadFacadeLocalImpl implements CurrentUserReadFacade {

    private final UserQueryApplicationService userQueryApplicationService;
    private final DepartmentQueryApplicationService departmentQueryApplicationService;
    private final PermissionQueryApplicationService permissionQueryService;

    public CurrentUserReadFacadeLocalImpl(
            UserQueryApplicationService userQueryApplicationService,
            DepartmentQueryApplicationService departmentQueryApplicationService,
            PermissionQueryApplicationService permissionQueryService) {
        this.userQueryApplicationService = userQueryApplicationService;
        this.departmentQueryApplicationService = departmentQueryApplicationService;
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public UserFacadeResponse getCurrentUser() {
        BaconContextHolder.requireTenantId();
        UserDTO user = userQueryApplicationService.getUserById(BaconIdContextHelper.requireUserId());
        return UserInterfaceAssembler.toFacadeResponse(user, resolveDepartmentCode(user));
    }

    @Override
    public TenantFacadeResponse getCurrentTenant() {
        BaconContextHolder.requireTenantId();
        return TenantInterfaceAssembler.toFacadeResponse(
                userQueryApplicationService.getTenantByTenantId(BaconIdContextHelper.requireTenantId()));
    }

    @Override
    public UserDataScopeFacadeResponse getCurrentDataScope() {
        BaconContextHolder.requireTenantId();
        UserDataScopeDTO dataScope = permissionQueryService.getUserDataScope(BaconIdContextHelper.requireUserId());
        return new UserDataScopeFacadeResponse(
                dataScope.isAllAccess(), dataScope.getScopeTypes(), dataScope.getDepartmentIds());
    }

    private String resolveDepartmentCode(UserDTO user) {
        if (user.getDepartmentId() == null) {
            return null;
        }
        DepartmentDTO department =
                departmentQueryApplicationService.getById(DepartmentIdCodec.toDomain(user.getDepartmentId()));
        return department.getCode();
    }
}
