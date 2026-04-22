package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import java.util.Set;

/**
 * OAuth 客户端响应对象。
 */
public record OAuthClientResponse(
        /** 客户端标识。 */
        String clientId,
        /** 客户端名称。 */
        String clientName,
        /** 支持的授权类型集合。 */
        Set<String> grantTypes,
        /** 支持的授权范围集合。 */
        Set<String> scopes,
        /** 回调地址集合。 */
        Set<String> redirectUris,
        /** 启用标记。 */
        boolean enabled) {

    public static OAuthClientResponse from(OAuthClientDTO dto) {
        if (dto == null) {
            return null;
        }
        return new OAuthClientResponse(
                dto.getClientId(),
                dto.getClientName(),
                dto.getGrantTypes(),
                dto.getScopes(),
                dto.getRedirectUris(),
                dto.isEnabled());
    }
}
