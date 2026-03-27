package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.api.dto.OAuth2UserinfoDTO;

/**
 * OAuth2 userinfo 响应对象。
 */
public record OAuth2UserinfoResponse(
        /** 用户主体标识。 */
        String sub,
        /** 租户标识。 */
        @JsonProperty("tenant_id") String tenantId,
        /** 用户名称。 */
        String name) {

    public static OAuth2UserinfoResponse from(OAuth2UserinfoDTO dto) {
        return new OAuth2UserinfoResponse(dto.getSub(), dto.getTenantId(), dto.getName());
    }
}
