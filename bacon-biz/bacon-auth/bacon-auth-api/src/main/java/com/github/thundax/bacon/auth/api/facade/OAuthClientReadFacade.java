package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;

public interface OAuthClientReadFacade {

    OAuthClientDTO getClientByClientId(String clientId);
}
