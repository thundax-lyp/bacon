package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientDTO {

    private String clientId;
    private String clientName;
    private Set<String> grantTypes;
    private Set<String> scopes;
    private Set<String> redirectUris;
    private boolean enabled;
}
