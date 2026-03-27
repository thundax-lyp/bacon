package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * OAuth2 客户端传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientDTO {

    /** 客户端标识。 */
    private String clientId;
    /** 客户端名称。 */
    private String clientName;
    /** 支持的授权类型集合。 */
    private Set<String> grantTypes;
    /** 支持的授权范围集合。 */
    private Set<String> scopes;
    /** 回调地址集合。 */
    private Set<String> redirectUris;
    /** 启用标记。 */
    private boolean enabled;
}
