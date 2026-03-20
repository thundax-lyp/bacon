package com.github.thundax.bacon.auth.domain.repository;

import com.github.thundax.bacon.auth.domain.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.entity.OAuthRefreshToken;
import java.util.Optional;

public interface OAuthAuthorizationRepository {

    OAuthAuthorizationRequest saveAuthorizationRequest(OAuthAuthorizationRequest authorizationRequest);

    Optional<OAuthAuthorizationRequest> findAuthorizationRequestById(String authorizationRequestId);

    void saveAuthorizationCode(String authorizationCode, OAuthAuthorizationRequest authorizationRequest);

    Optional<OAuthAuthorizationRequest> findAuthorizationRequestByCode(String authorizationCode);

    OAuthAccessToken saveAccessToken(OAuthAccessToken accessToken);

    Optional<OAuthAccessToken> findAccessTokenByHash(String tokenHash);

    OAuthRefreshToken saveOAuthRefreshToken(OAuthRefreshToken refreshToken);

    Optional<OAuthRefreshToken> findOAuthRefreshTokenByHash(String tokenHash);
}
