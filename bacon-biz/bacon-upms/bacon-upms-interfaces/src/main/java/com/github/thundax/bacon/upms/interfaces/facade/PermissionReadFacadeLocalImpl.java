package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.application.query.PermissionQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PermissionReadFacadeLocalImpl implements PermissionReadFacade {

    private final PermissionQueryApplicationService permissionQueryService;

    public PermissionReadFacadeLocalImpl(PermissionQueryApplicationService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(String tenantId, String userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @Override
    public Set<String> getUserPermissionCodes(String tenantId, String userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(String tenantId, String userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
