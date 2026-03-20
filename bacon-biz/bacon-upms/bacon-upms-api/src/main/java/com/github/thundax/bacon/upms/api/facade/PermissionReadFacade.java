package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import java.util.List;
import java.util.Set;

public interface PermissionReadFacade {

    List<UserMenuTreeDTO> getUserMenuTree(Long tenantId, Long userId);

    Set<String> getUserPermissionCodes(Long tenantId, Long userId);

    UserDataScopeDTO getUserDataScope(Long tenantId, Long userId);
}
