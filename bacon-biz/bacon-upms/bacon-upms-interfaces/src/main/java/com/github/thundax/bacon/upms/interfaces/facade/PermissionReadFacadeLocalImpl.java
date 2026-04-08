package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.application.command.TenantApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PermissionReadFacadeLocalImpl implements PermissionReadFacade {

    private final PermissionQueryApplicationService permissionQueryService;
    private final TenantApplicationService tenantApplicationService;

    public PermissionReadFacadeLocalImpl(PermissionQueryApplicationService permissionQueryService,
                                         TenantApplicationService tenantApplicationService) {
        this.permissionQueryService = permissionQueryService;
        this.tenantApplicationService = tenantApplicationService;
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(@NonNull TenantId tenantId, @NonNull UserId userId) {
        return permissionQueryService.getUserMenuTree(resolveExistingTenantId(tenantId), userId);
    }

    @Override
    public Set<String> getUserPermissionCodes(@NonNull TenantId tenantId, @NonNull UserId userId) {
        return permissionQueryService.getUserPermissionCodes(resolveExistingTenantId(tenantId), userId);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(@NonNull TenantId tenantId, @NonNull UserId userId) {
        return permissionQueryService.getUserDataScope(resolveExistingTenantId(tenantId), userId);
    }

    private TenantId resolveExistingTenantId(TenantId tenantId) {
        return tenantApplicationService.getTenantByTenantId(tenantId.value()).getId();
    }
}
