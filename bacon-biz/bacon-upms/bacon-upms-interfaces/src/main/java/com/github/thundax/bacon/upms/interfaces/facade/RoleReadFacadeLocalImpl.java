package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class RoleReadFacadeLocalImpl implements RoleReadFacade {

    private final RoleApplicationService roleApplicationService;

    public RoleReadFacadeLocalImpl(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    @Override
    public RoleDTO getRoleById(@NonNull TenantId tenantId, @NonNull RoleId roleId) {
        return roleApplicationService.getRoleById(tenantId, roleId);
    }

    @Override
    public List<RoleDTO> getRolesByUserId(@NonNull TenantId tenantId, @NonNull UserId userId) {
        return roleApplicationService.getRolesByUserId(tenantId, userId);
    }
}
