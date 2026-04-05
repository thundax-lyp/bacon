package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.application.command.TenantApplicationService;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    public List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, Long userId) {
        return permissionQueryService.getUserMenuTree(requireExistingTenantId(tenantId), UserId.of(userId));
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        return permissionQueryService.getUserPermissionCodes(requireExistingTenantId(tenantId), UserId.of(userId));
    }

    @Override
    public UserDataScopeDTO getUserDataScope(Long tenantId, Long userId) {
        return permissionQueryService.getUserDataScope(requireExistingTenantId(tenantId), UserId.of(userId));
    }

    private TenantId requireExistingTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        return tenantApplicationService.getTenantByTenantId(TenantId.of(tenantId)).getId();
    }
}
