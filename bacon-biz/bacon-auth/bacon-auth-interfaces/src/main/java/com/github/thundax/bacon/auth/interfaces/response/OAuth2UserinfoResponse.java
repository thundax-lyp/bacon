package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.api.dto.OAuth2UserinfoDTO;

public record OAuth2UserinfoResponse(String sub, @JsonProperty("tenant_id") String tenantId, String name) {

    public static OAuth2UserinfoResponse from(OAuth2UserinfoDTO dto) {
        return new OAuth2UserinfoResponse(dto.getSub(), dto.getTenantId(), dto.getName());
    }
}
