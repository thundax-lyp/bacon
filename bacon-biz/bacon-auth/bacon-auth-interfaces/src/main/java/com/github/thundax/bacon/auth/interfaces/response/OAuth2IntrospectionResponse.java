package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.api.dto.OAuth2IntrospectionDTO;

public record OAuth2IntrospectionResponse(boolean active, @JsonProperty("client_id") String clientId,
                                          String scope, String sub, @JsonProperty("tenant_id") String tenantId,
                                          long exp) {

    public static OAuth2IntrospectionResponse from(OAuth2IntrospectionDTO dto) {
        return new OAuth2IntrospectionResponse(dto.isActive(), dto.getClientId(), dto.getScope(), dto.getSub(),
                dto.getTenantId(), dto.getExp());
    }
}
