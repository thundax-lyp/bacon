package com.github.thundax.bacon.auth.domain.model.entity;

import java.time.Instant;
import java.util.Set;

public record OAuthClient(
        Long id,
        String clientId,
        String clientSecret,
        String clientName,
        String clientType,
        Set<String> grantTypes,
        Set<String> scopes,
        Set<String> redirectUris,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds,
        boolean enabled,
        String contact,
        String remark,
        Instant createdAt,
        Instant updatedAt) {
}
