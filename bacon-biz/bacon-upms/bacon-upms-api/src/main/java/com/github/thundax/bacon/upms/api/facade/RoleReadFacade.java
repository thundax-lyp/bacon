package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import org.springframework.lang.NonNull;

public interface RoleReadFacade {

    RoleDTO getRoleById(@NonNull TenantId tenantId, @NonNull RoleId roleId);

    List<RoleDTO> getRolesByUserId(@NonNull TenantId tenantId, @NonNull UserId userId);
}
