package com.github.thundax.bacon.auth.api.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
