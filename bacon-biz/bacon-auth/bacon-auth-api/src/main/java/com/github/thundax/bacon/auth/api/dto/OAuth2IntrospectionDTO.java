package com.github.thundax.bacon.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String tenantId;
    private long exp;
}
