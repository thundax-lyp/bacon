package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.api.dto.OAuth2TokenDTO;

public record OAuth2TokenResponse(@JsonProperty("access_token") String accessToken,
                                  @JsonProperty("token_type") String tokenType,
                                  @JsonProperty("expires_in") long expiresIn,
                                  @JsonProperty("refresh_token") String refreshToken,
                                  String scope) {

    public static OAuth2TokenResponse from(OAuth2TokenDTO dto) {
        return new OAuth2TokenResponse(dto.getAccessToken(), dto.getTokenType(), dto.getExpiresIn(),
                dto.getRefreshToken(), dto.getScope());
    }
}
