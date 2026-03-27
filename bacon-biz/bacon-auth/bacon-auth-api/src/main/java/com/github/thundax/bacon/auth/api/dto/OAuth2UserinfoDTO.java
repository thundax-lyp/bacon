package com.github.thundax.bacon.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 userinfo 传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserinfoDTO {

    /** 用户主体标识。 */
    private String sub;
    /** 租户标识。 */
    @JsonProperty("tenant_id")
    private String tenantId;
    /** 用户名称。 */
    private String name;
}
