package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryOAuthAuthorizationRepositoryImpl implements OAuthAuthorizationRepository {

    private final TestAuthMemoryStore authStore;

    public InMemoryOAuthAuthorizationRepositoryImpl(TestAuthMemoryStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public OAuthAuthorizationRequest update(OAuthAuthorizationRequest authorizationRequest) {
        authStore
                .getAuthorizationRequests()
                .put(authorizationRequest.getAuthorizationRequestId(), authorizationRequest);
        return authorizationRequest;
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findById(String authorizationRequestId) {
        return Optional.ofNullable(authStore.getAuthorizationRequests().get(authorizationRequestId));
    }

    @Override
    public void insertCode(String authorizationCode, OAuthAuthorizationRequest authorizationRequest) {
        authStore.getAuthorizationCodes().put(authorizationCode, authorizationRequest);
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findByCode(String authorizationCode) {
        return Optional.ofNullable(authStore.getAuthorizationCodes().get(authorizationCode));
    }

    @Override
    public OAuthAccessToken update(OAuthAccessToken accessToken) {
        authStore.getAccessTokens().put(accessToken.getTokenHash(), accessToken);
        return accessToken;
    }

    @Override
    public Optional<OAuthAccessToken> findAccessByHash(String tokenHash) {
        return Optional.ofNullable(authStore.getAccessTokens().get(tokenHash));
    }

    @Override
    public OAuthRefreshToken update(OAuthRefreshToken refreshToken) {
        authStore.getOauthRefreshTokens().put(refreshToken.getTokenHash(), refreshToken);
        return refreshToken;
    }

    @Override
    public Optional<OAuthRefreshToken> findByHash(String tokenHash) {
        return Optional.ofNullable(authStore.getOauthRefreshTokens().get(tokenHash));
    }
}
