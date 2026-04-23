package com.github.thundax.bacon.auth.api.response;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientFacadeResponse {

    private String clientId;
    private String clientName;
    private Set<String> grantTypes;
    private Set<String> scopes;
    private Set<String> redirectUris;
    private boolean enabled;

    public static OAuthClientFacadeResponse from(OAuthClientDTO dto) {
        if (dto == null) {
            return null;
        }
        return new OAuthClientFacadeResponse(
                dto.getClientId(),
                dto.getClientName(),
                dto.getGrantTypes(),
                dto.getScopes(),
                dto.getRedirectUris(),
                dto.isEnabled());
    }
}
