package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2IntrospectionDTO {

    private boolean active;
    private String client_id;
    private String scope;
    private String sub;
    private String tenant_id;
    private long exp;
}
