package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import org.springframework.lang.NonNull;

public interface UserReadFacade {

    UserDTO getUserById(@NonNull UserId userId);

    UserIdentityDTO getUserIdentity(UserIdentityType identityType, String identityValue);

    UserLoginCredentialDTO getUserLoginCredential(UserIdentityType identityType, String identityValue);

    TenantDTO getTenantByTenantId();
}
