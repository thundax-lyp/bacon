package com.github.thundax.bacon.auth.application.assembler;

import com.github.thundax.bacon.auth.application.dto.UserLoginDTO;

public final class LoginAssembler {

    private LoginAssembler() {}

    public static UserLoginDTO toUserLoginDto(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String sessionId,
            Long userId,
            Long tenantId,
            Boolean needChangePassword) {
        return new UserLoginDTO(
                accessToken, refreshToken, tokenType, expiresIn, sessionId, userId, tenantId, needChangePassword);
    }
}
