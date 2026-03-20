package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;

public interface UserReadFacade {

    UserDTO getUserById(Long tenantId, Long userId);

    UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue);

    TenantDTO getTenantByTenantId(Long tenantId);
}
