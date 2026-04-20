package com.github.thundax.bacon.upms.api.response;

import java.time.Instant;
import java.util.List;

public record UserCredentialDetailFacadeResponse(
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
        String passwordHash) {}
