package com.github.thundax.bacon.auth.domain.repository;

import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import java.util.Optional;

public interface OAuthAuthorizationRepository {

    OAuthAuthorizationRequest update(OAuthAuthorizationRequest authorizationRequest);

    Optional<OAuthAuthorizationRequest> findById(String authorizationRequestId);

    void insertCode(String authorizationCode, OAuthAuthorizationRequest authorizationRequest);

    Optional<OAuthAuthorizationRequest> findByCode(String authorizationCode);

    OAuthAccessToken update(OAuthAccessToken accessToken);

    Optional<OAuthAccessToken> findAccessByHash(String tokenHash);

    OAuthRefreshToken update(OAuthRefreshToken refreshToken);

    Optional<OAuthRefreshToken> findByHash(String tokenHash);
}
