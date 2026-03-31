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
    public List<UserMenuTreeDTO> getUserMenuTree(String tenantNo, Long userId) {
        return permissionQueryService.getUserMenuTree(tenantNo, userId);
    }

    @Override
    public Set<String> getUserPermissionCodes(String tenantNo, Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantNo, userId);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(String tenantNo, Long userId) {
        return permissionQueryService.getUserDataScope(tenantNo, userId);
    }
}
