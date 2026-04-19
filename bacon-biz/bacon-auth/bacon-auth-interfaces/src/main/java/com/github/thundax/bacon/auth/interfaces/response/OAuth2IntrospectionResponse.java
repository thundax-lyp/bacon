package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.application.dto.OAuth2IntrospectionDTO;

/**
 * OAuth2 introspection 响应对象。
 */
public record OAuth2IntrospectionResponse(
        /** 令牌是否有效。 */
        boolean active,
        /** 客户端标识。 */
        @JsonProperty("client_id") String clientId,
        /** 授权范围。 */
        String scope,
        /** 用户主体标识。 */
        String sub,
        /** 租户标识。 */
        @JsonProperty("tenant_id") Long tenantId,
        /** 过期时间戳。 */
        long exp) {

    public static OAuth2IntrospectionResponse from(OAuth2IntrospectionDTO dto) {
        return new OAuth2IntrospectionResponse(
                dto.isActive(), dto.getClientId(), dto.getScope(), dto.getSub(), dto.getTenantId(), dto.getExp());
    }
}
