package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;

import java.util.List;

public interface RoleReadFacade {

    RoleDTO getRoleById(String tenantNo, Long roleId);

    List<RoleDTO> getRolesByUserId(String tenantNo, Long userId);
}
