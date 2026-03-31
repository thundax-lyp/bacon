package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;

public interface UserReadFacade {

    UserDTO getUserById(String tenantNo, Long userId);

    UserIdentityDTO getUserIdentity(String tenantNo, String identityType, String identityValue);

    UserLoginCredentialDTO getUserLoginCredential(String tenantNo, String identityType, String identityValue);

    TenantDTO getTenantByTenantNo(String tenantNo);
}
