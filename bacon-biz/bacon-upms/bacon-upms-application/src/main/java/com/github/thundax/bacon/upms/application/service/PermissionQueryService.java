package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PermissionQueryService {

    private final PermissionRepository permissionRepository;
    private final MenuApplicationService menuApplicationService;

    public PermissionQueryService(PermissionRepository permissionRepository, MenuApplicationService menuApplicationService) {
        this.permissionRepository = permissionRepository;
        this.menuApplicationService = menuApplicationService;
    }

    public List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, Long userId) {
        return menuApplicationService.toMenuTree(permissionRepository.getUserMenuTree(tenantId, userId));
    }

    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        return permissionRepository.getUserPermissionCodes(tenantId, userId);
    }

    public UserDataScopeDTO getUserDataScope(Long tenantId, Long userId) {
        return new UserDataScopeDTO(permissionRepository.hasAllAccess(tenantId, userId),
                permissionRepository.getUserScopeTypes(tenantId, userId),
                permissionRepository.getUserDepartmentIds(tenantId, userId));
    }
}
