package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;

import java.util.List;

public interface RoleReadFacade {

    RoleDTO getRoleById(String tenantId, String roleId);

    List<RoleDTO> getRolesByUserId(String tenantId, String userId);
}
