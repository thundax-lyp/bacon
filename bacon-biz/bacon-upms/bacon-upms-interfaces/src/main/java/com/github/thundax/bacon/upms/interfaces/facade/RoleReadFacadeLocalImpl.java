package com.github.thundax.bacon.upms.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.api.request.RoleGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.RoleListByUserFacadeRequest;
import com.github.thundax.bacon.upms.api.response.RoleFacadeResponse;
import com.github.thundax.bacon.upms.api.response.RoleListFacadeResponse;
import com.github.thundax.bacon.upms.application.command.RoleApplicationService;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
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
    public RoleFacadeResponse getRoleById(RoleGetFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return RoleFacadeResponse.from(roleApplicationService.getRoleById(RoleId.of(request.getRoleId())));
    }

    @Override
    public RoleListFacadeResponse getRolesByUserId(RoleListByUserFacadeRequest request) {
        BaconContextHolder.requireTenantId();
        return RoleListFacadeResponse.from(roleApplicationService.getRolesByUserId(UserId.of(request.getUserId())));
    }
}
