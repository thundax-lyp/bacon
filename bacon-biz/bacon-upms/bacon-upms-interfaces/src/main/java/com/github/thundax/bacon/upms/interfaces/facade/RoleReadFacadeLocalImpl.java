package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
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
    public RoleDTO getRoleById(Long tenantId, Long roleId) {
        return roleApplicationService.getRoleById(TenantId.of(tenantId), RoleId.of(roleId));
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        return roleApplicationService.getRolesByUserId(TenantId.of(tenantId), UserId.of(userId));
    }
}
