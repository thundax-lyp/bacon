package com.github.thundax.bacon.auth.application.assembler;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;

public final class OAuth2ClientAssembler {

    private OAuth2ClientAssembler() {}

    public static OAuthClientDTO toDto(OAuthClient client) {
        return new OAuthClientDTO(
                client.getClientCodeValue(),
                client.getClientName(),
                client.getGrantTypes(),
                client.getScopes(),
                client.getRedirectUris(),
                client.isEnabled());
    }
}
