package com.github.thundax.bacon.upms.interfaces.provider;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.application.service.RoleApplicationService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class RoleReadFacadeLocalImpl implements RoleReadFacade {

    private final RoleApplicationService roleApplicationService;

    public RoleReadFacadeLocalImpl(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @Override
    public RoleDTO getRoleById(Long tenantId, Long roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
