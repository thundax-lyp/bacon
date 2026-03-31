package com.github.thundax.bacon.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 introspection 传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2IntrospectionDTO {

    /** 令牌是否有效。 */
    private boolean active;
    /** 客户端标识。 */
    @JsonProperty("client_id")
    private String clientId;
    /** 授权范围。 */
    private String scope;
    /** 用户主体标识。 */
    private String sub;
    /** 租户标识。 */
    @JsonProperty("tenant_id")
    private String tenantNo;
    /** 过期时间戳。 */
    private long exp;
}
