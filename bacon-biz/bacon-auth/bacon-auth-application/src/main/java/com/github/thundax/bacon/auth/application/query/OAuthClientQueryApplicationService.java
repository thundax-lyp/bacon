package com.github.thundax.bacon.auth.application.query;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.application.assembler.OAuth2ClientAssembler;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OAuthClientQueryApplicationService {

    private final OAuthClientRepository oAuthClientRepository;

    public OAuthClientQueryApplicationService(OAuthClientRepository oAuthClientRepository) {
        this.oAuthClientRepository = oAuthClientRepository;
    }

    public OAuthClientDTO getClientByClientId(OAuthClientQuery query) {
        OAuthClient client = oAuthClientRepository
                .findByClientCode(query.clientId())
                .orElseThrow(() -> new NotFoundException("OAuth client not found: " + query.clientId()));
        return OAuth2ClientAssembler.toDto(client);
    }
}
