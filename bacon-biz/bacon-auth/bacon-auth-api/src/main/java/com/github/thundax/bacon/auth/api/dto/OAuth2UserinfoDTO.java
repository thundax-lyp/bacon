package com.github.thundax.bacon.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserinfoDTO {

    private String sub;
    @JsonProperty("tenant_id")
    private String tenantId;
    private String name;
}
