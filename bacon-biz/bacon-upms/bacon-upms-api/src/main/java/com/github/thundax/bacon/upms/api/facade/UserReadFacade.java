package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import org.springframework.lang.NonNull;

public interface UserReadFacade {

    UserDTO getUserById(@NonNull TenantId tenantId, @NonNull UserId userId);

    UserIdentityDTO getUserIdentity(@NonNull TenantId tenantId, String identityType, String identityValue);

    UserLoginCredentialDTO getUserLoginCredential(
            @NonNull TenantId tenantId, String identityType, String identityValue);

    TenantDTO getTenantByTenantId(@NonNull TenantId tenantId);
}
