package com.github.thundax.bacon.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 introspection 应用层模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2IntrospectionDTO {

    private boolean active;

    @JsonProperty("client_id")
    private String clientId;

    private String scope;
    private String sub;

    @JsonProperty("tenant_id")
    private Long tenantId;

    private long exp;
}
