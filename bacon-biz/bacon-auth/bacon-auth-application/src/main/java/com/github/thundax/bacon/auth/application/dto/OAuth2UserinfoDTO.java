package com.github.thundax.bacon.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 userinfo 应用层模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserinfoDTO {

    private String sub;

    @JsonProperty("tenant_id")
    private Long tenantId;

    private String name;
}
