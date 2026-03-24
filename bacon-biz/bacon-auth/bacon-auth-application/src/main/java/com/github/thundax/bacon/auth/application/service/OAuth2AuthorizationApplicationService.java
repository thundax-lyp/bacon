package com.github.thundax.bacon.auth.application.service;

import com.github.thundax.bacon.auth.api.dto.OAuth2IntrospectionDTO;
import com.github.thundax.bacon.auth.api.dto.OAuth2TokenDTO;
import com.github.thundax.bacon.auth.api.dto.OAuth2UserinfoDTO;
import com.github.thundax.bacon.auth.domain.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.common.core.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationApplicationService {

    private final OAuthClientRepository oAuthClientRepository;
    private final OAuthAuthorizationRepository oAuthAuthorizationRepository;
    private final SessionApplicationService sessionApplicationService;
    private final TokenCodec tokenCodec;

    public OAuth2AuthorizationApplicationService(OAuthClientRepository oAuthClientRepository,
                                                 OAuthAuthorizationRepository oAuthAuthorizationRepository,
                                                 SessionApplicationService sessionApplicationService,
                                                 TokenCodec tokenCodec) {
        this.oAuthClientRepository = oAuthClientRepository;
        this.oAuthAuthorizationRepository = oAuthAuthorizationRepository;
        this.sessionApplicationService = sessionApplicationService;
        this.tokenCodec = tokenCodec;
    }

    public AuthorizationView authorize(String accessToken, String clientId, String redirectUri, String scope, String state,
                                       String codeChallenge, String codeChallengeMethod) {
        OAuthClient client = loadClient(clientId);
        if (!client.getRedirectUris().contains(redirectUri)) {
            throw new IllegalArgumentException("Redirect uri invalid");
        }
        Set<String> scopes = splitScopes(scope);
        if (!client.getScopes().containsAll(scopes)) {
            throw new IllegalArgumentException("Scope invalid");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException("Login required before OAuth2 authorization");
        }
        var currentSession = sessionApplicationService.currentSession(accessToken);
        Long tenantId = currentSession.getTenantId();
        Long userId = currentSession.getUserId();

        String authorizationRequestId = UUID.randomUUID().toString();
        oAuthAuthorizationRepository.saveAuthorizationRequest(new OAuthAuthorizationRequest(
                authorizationRequestId, clientId, redirectUri, scopes, state, codeChallenge, codeChallengeMethod,
                tenantId, userId, Instant.now().plusSeconds(300)));
        return new AuthorizationView(authorizationRequestId, client.getClientId(), client.getClientName(), scope, state);
    }

    public AuthorizationDecisionResult decide(String authorizationRequestId, String decision) {
        OAuthAuthorizationRequest authorizationRequest = oAuthAuthorizationRepository.findAuthorizationRequestById(authorizationRequestId)
                .filter(request -> !request.isUsed())
                .filter(request -> request.getExpireAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Authorization request invalid"));
        if (!"APPROVE".equalsIgnoreCase(decision) && !"REJECT".equalsIgnoreCase(decision)) {
            throw new IllegalArgumentException("Decision invalid");
        }
        authorizationRequest.markUsed();
        if ("REJECT".equalsIgnoreCase(decision)) {
            return new AuthorizationDecisionResult(authorizationRequest.getRedirectUri()
                    + "?error=access_denied&state=" + authorizationRequest.getState(), null);
        }

        String authorizationCode = tokenCodec.randomToken();
        oAuthAuthorizationRepository.saveAuthorizationCode(authorizationCode, authorizationRequest);
        return new AuthorizationDecisionResult(authorizationRequest.getRedirectUri()
                + "?code=" + authorizationCode + "&state=" + authorizationRequest.getState(), authorizationCode);
    }

    public OAuth2TokenDTO token(String grantType, String code, String redirectUri, String clientId,
                                     String clientSecret, String codeVerifier, String refreshToken) {
        OAuthClient client = validateClient(clientId, clientSecret);
        if ("authorization_code".equals(grantType)) {
            OAuthAuthorizationRequest request = oAuthAuthorizationRepository.findAuthorizationRequestByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Authorization code invalid"));
            if (!request.getRedirectUri().equals(redirectUri)) {
                throw new IllegalArgumentException("Redirect uri invalid");
            }
            return issueOAuthTokens(client, request.getTenantId(), request.getUserId(), request.getScopes());
        }
        if ("refresh_token".equals(grantType)) {
            OAuthRefreshToken currentRefreshToken = oAuthAuthorizationRepository.findOAuthRefreshTokenByHash(tokenCodec.sha256(refreshToken))
                    .filter(token -> "ACTIVE".equals(token.getTokenStatus()))
                    .orElseThrow(() -> new IllegalArgumentException("OAuth refresh token invalid"));
            currentRefreshToken.markUsed();
            Optional<OAuthAccessToken> accessToken = oAuthAuthorizationRepository.findAccessTokenByHash(currentRefreshToken.getAccessTokenId());
            Set<String> scopes = accessToken.map(OAuthAccessToken::getScopes).orElseGet(LinkedHashSet::new);
            return issueOAuthTokens(client, currentRefreshToken.getTenantId(), currentRefreshToken.getUserId(), scopes);
        }
        throw new IllegalArgumentException("Grant type unsupported");
    }

    public OAuth2IntrospectionDTO introspect(String token, String clientId, String clientSecret) {
        validateClient(clientId, clientSecret);
        return oAuthAuthorizationRepository.findAccessTokenByHash(tokenCodec.sha256(token))
                .filter(accessToken -> "ACTIVE".equals(accessToken.getTokenStatus()))
                .filter(accessToken -> accessToken.getExpireAt().isAfter(Instant.now()))
                .map(accessToken -> new OAuth2IntrospectionDTO(true, accessToken.getClientId(),
                        String.join(" ", accessToken.getScopes()), String.valueOf(accessToken.getUserId()),
                        String.valueOf(accessToken.getTenantId()), accessToken.getExpireAt().getEpochSecond()))
                .orElse(new OAuth2IntrospectionDTO(false, clientId, "", "", "", 0L));
    }

    public void revoke(String token, String clientId, String clientSecret) {
        validateClient(clientId, clientSecret);
        oAuthAuthorizationRepository.findAccessTokenByHash(tokenCodec.sha256(token)).ifPresent(OAuthAccessToken::revoke);
        oAuthAuthorizationRepository.findOAuthRefreshTokenByHash(tokenCodec.sha256(token)).ifPresent(OAuthRefreshToken::revoke);
    }

    public OAuth2UserinfoDTO userinfo(String accessToken) {
        OAuthAccessToken token = oAuthAuthorizationRepository.findAccessTokenByHash(tokenCodec.sha256(accessToken))
                .filter(current -> "ACTIVE".equals(current.getTokenStatus()))
                .orElseThrow(() -> new IllegalArgumentException("OAuth access token invalid"));
        String name = token.getScopes().contains("profile") ? "demo-user-" + token.getUserId() : null;
        return new OAuth2UserinfoDTO(String.valueOf(token.getUserId()), String.valueOf(token.getTenantId()), name);
    }

    private OAuth2TokenDTO issueOAuthTokens(OAuthClient client, Long tenantId, Long userId, Set<String> scopes) {
        Instant now = Instant.now();
        String accessTokenValue = tokenCodec.randomToken();
        String accessTokenId = tokenCodec.sha256(accessTokenValue);
        OAuthAccessToken accessToken = new OAuthAccessToken(accessTokenId, accessTokenId, client.getClientId(), tenantId,
                userId, scopes, now, now.plusSeconds(client.getAccessTokenTtlSeconds()));
        oAuthAuthorizationRepository.saveAccessToken(accessToken);

        String refreshTokenValue = tokenCodec.randomToken();
        String refreshTokenHash = tokenCodec.sha256(refreshTokenValue);
        OAuthRefreshToken refreshToken = new OAuthRefreshToken(refreshTokenHash, refreshTokenHash, accessTokenId,
                client.getClientId(), tenantId, userId, now, now.plusSeconds(client.getRefreshTokenTtlSeconds()));
        oAuthAuthorizationRepository.saveOAuthRefreshToken(refreshToken);

        return new OAuth2TokenDTO(accessTokenValue, "Bearer", client.getAccessTokenTtlSeconds(),
                refreshTokenValue, String.join(" ", scopes));
    }

    private OAuthClient loadClient(String clientId) {
        return oAuthClientRepository.findByClientId(clientId)
                .filter(OAuthClient::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("OAuth client invalid"));
    }

    private OAuthClient validateClient(String clientId, String clientSecret) {
        OAuthClient client = loadClient(clientId);
        if (!client.getClientSecret().equals(clientSecret)) {
            throw new IllegalArgumentException("OAuth client secret invalid");
        }
        return client;
    }

    private Set<String> splitScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(scope.trim().split("\\s+"))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthorizationView {

        private String authorizationRequestId;
        private String clientId;
        private String clientName;
        private String scope;
        private String state;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthorizationDecisionResult {

        private String redirectUri;
        private String authorizationCode;
    }
}
