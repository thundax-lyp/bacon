package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class RoleReadFacadeLocalImpl implements RoleReadFacade {

    private final RoleApplicationService roleApplicationService;

    public RoleReadFacadeLocalImpl(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @Override
    public RoleDTO getRoleById(String tenantId, String roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(String tenantId, String userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
