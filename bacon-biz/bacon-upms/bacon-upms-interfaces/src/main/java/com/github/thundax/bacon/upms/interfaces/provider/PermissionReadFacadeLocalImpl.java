package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.application.service.PermissionQueryService;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class PermissionReadFacadeLocalImpl implements PermissionReadFacade {

    private final PermissionQueryService permissionQueryService;

    public PermissionReadFacadeLocalImpl(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, Long userId) {
        return permissionQueryService.getUserMenuTree(tenantId, userId);
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        return permissionQueryService.getUserPermissionCodes(tenantId, userId);
    }

    @Override
    public UserDataScopeDTO getUserDataScope(Long tenantId, Long userId) {
        return permissionQueryService.getUserDataScope(tenantId, userId);
    }
}
