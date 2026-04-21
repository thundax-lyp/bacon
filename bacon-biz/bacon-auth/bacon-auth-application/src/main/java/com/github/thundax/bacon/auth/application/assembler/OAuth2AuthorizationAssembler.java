package com.github.thundax.bacon.auth.application.assembler;

import com.github.thundax.bacon.auth.application.dto.OAuth2IntrospectionDTO;
import com.github.thundax.bacon.auth.application.dto.OAuth2TokenDTO;
import com.github.thundax.bacon.auth.application.dto.OAuth2UserinfoDTO;

public final class OAuth2AuthorizationAssembler {

    private OAuth2AuthorizationAssembler() {}

    public static OAuth2IntrospectionDTO toIntrospectionDto(
            boolean active, String clientId, String scope, String sub, Long tenantId, long exp) {
        return new OAuth2IntrospectionDTO(active, clientId, scope, sub, tenantId, exp);
    }

    public static OAuth2TokenDTO toOAuth2TokenDto(
            String accessToken, String tokenType, long expiresIn, String refreshToken, String scope) {
        return new OAuth2TokenDTO(accessToken, tokenType, expiresIn, refreshToken, scope);
    }

    public static OAuth2UserinfoDTO toUserinfoDto(String sub, Long tenantId, String name) {
        return new OAuth2UserinfoDTO(sub, tenantId, name);
    }
}
