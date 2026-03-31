package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;

import java.util.List;
import java.util.Set;

public interface PermissionReadFacade {

    List<UserMenuTreeDTO> getUserMenuTree(String tenantId, String userId);

    Set<String> getUserPermissionCodes(String tenantId, String userId);

    UserDataScopeDTO getUserDataScope(String tenantId, String userId);
}
