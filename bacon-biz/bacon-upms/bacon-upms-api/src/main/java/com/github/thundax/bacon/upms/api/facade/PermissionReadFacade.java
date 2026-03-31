package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;

import java.util.List;
import java.util.Set;

public interface PermissionReadFacade {

    List<UserMenuTreeDTO> getUserMenuTree(String tenantNo, String userId);

    Set<String> getUserPermissionCodes(String tenantNo, String userId);

    UserDataScopeDTO getUserDataScope(String tenantNo, String userId);
}
