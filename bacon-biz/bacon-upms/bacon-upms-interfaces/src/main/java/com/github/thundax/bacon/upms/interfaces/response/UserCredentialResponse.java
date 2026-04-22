package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import java.time.Instant;
import java.util.List;

/**
 * 用户登录凭据响应对象。
 */
public record UserCredentialResponse(
        Long userId,
        Long identityId,
        String account,
        String phone,
        String identityType,
        String identityValue,
        String identityStatus,
        Long credentialId,
        String credentialType,
        String credentialStatus,
        boolean needChangePassword,
        Instant credentialExpiresAt,
        Instant lockedUntil,
        boolean mfaRequired,
        List<String> secondFactorTypes,
        String status,
        String passwordHash) {

    public static UserCredentialResponse from(UserLoginCredentialDTO dto) {
        return new UserCredentialResponse(
                dto.getUserId(),
                dto.getIdentityId(),
                dto.getAccount(),
                dto.getPhone(),
                dto.getIdentityType(),
                dto.getIdentityValue(),
                dto.getIdentityStatus(),
                dto.getCredentialId(),
                dto.getCredentialType(),
                dto.getCredentialStatus(),
                dto.isNeedChangePassword(),
                dto.getCredentialExpiresAt(),
                dto.getLockedUntil(),
                dto.isMfaRequired(),
                dto.getSecondFactorTypes(),
                dto.getStatus(),
                dto.getPasswordHash());
    }
}
