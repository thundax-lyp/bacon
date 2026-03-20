package com.github.thundax.bacon.auth.api.dto;

import java.util.Set;

public record OAuthClientDTO(
        String clientId,
        String clientName,
        Set<String> grantTypes,
        Set<String> scopes,
        Set<String> redirectUris,
        boolean enabled) {
}
