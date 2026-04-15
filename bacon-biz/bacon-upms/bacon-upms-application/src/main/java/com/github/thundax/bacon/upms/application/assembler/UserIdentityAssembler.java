package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import java.util.List;

public final class UserIdentityAssembler {

    private UserIdentityAssembler() {}

    public static UserIdentityDTO toDto(UserIdentity userIdentity) {
        return new UserIdentityDTO(
                userIdentity.getId() == null ? null : userIdentity.getId().value(),
                userIdentity.getUserId().value(),
                userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value());
    }

    public static UserLoginCredentialDTO toLoginCredentialDto(
            User user, UserIdentity userIdentity, UserCredential passwordCredential, String account, String phone) {
        return new UserLoginCredentialDTO(
                user.getId().value(),
                userIdentity.getId() == null ? null : userIdentity.getId().value(),
                account,
                phone,
                userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value(),
                passwordCredential.getId() == null
                        ? null
                        : passwordCredential.getId().value(),
                passwordCredential.getCredentialType().value(),
                passwordCredential.getStatus().value(),
                passwordCredential.isNeedChangePassword(),
                passwordCredential.getExpiresAt(),
                passwordCredential.getLockedUntil(),
                false,
                List.of(),
                user.getStatus().value(),
                passwordCredential.getCredentialValue());
    }
}
