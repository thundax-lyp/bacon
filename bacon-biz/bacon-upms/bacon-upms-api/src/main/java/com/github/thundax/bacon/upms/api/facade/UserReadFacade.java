package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;

public interface UserReadFacade {

    UserDTO getUserById(String tenantId, String userId);

    UserIdentityDTO getUserIdentity(String tenantId, String identityType, String identityValue);

    UserLoginCredentialDTO getUserLoginCredential(String tenantId, String identityType, String identityValue);

    TenantDTO getTenantByTenantId(String tenantId);
}
