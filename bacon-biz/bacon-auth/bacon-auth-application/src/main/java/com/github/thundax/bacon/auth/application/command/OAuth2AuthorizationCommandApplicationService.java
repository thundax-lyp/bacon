package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.assembler.OAuth2AuthorizationAssembler;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.OAuth2TokenDTO;
import com.github.thundax.bacon.auth.application.query.SessionCurrentQuery;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2AuthorizationCommandApplicationService {

    private final OAuthClientRepository oAuthClientRepository;
    private final OAuthAuthorizationRepository oAuthAuthorizationRepository;
    private final SessionQueryApplicationService sessionQueryApplicationService;
    private final TokenCodec tokenCodec;
    private final PasswordEncoder passwordEncoder;

    public OAuth2AuthorizationCommandApplicationService(
            OAuthClientRepository oAuthClientRepository,
            OAuthAuthorizationRepository oAuthAuthorizationRepository,
            SessionQueryApplicationService sessionQueryApplicationService,
            TokenCodec tokenCodec,
            PasswordEncoder passwordEncoder) {
        this.oAuthClientRepository = oAuthClientRepository;
        this.oAuthAuthorizationRepository = oAuthAuthorizationRepository;
        this.sessionQueryApplicationService = sessionQueryApplicationService;
        this.tokenCodec = tokenCodec;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthorizationView authorize(OAuth2AuthorizeCommand command) {
        OAuthClient client = loadClient(command.clientId());
        if (!client.getRedirectUris().contains(command.redirectUri())) {
            throw new AuthDomainException(AuthErrorCode.OAUTH_REDIRECT_URI_INVALID);
        }
        Set<String> scopes = splitScopes(command.scope());
        if (!client.getScopes().containsAll(scopes)) {
            throw new AuthDomainException(AuthErrorCode.OAUTH_SCOPE_INVALID);
        }

        if (command.accessToken() == null || command.accessToken().isBlank()) {
            throw new AuthDomainException(AuthErrorCode.OAUTH_LOGIN_REQUIRED);
        }
        var currentSession = sessionQueryApplicationService.currentSession(new SessionCurrentQuery(command.accessToken()));
        Long tenantId = currentSession.getTenantId();
        Long userId = currentSession.getUserId();

        String authorizationRequestId = UUID.randomUUID().toString();
        oAuthAuthorizationRepository.update(new OAuthAuthorizationRequest(
                authorizationRequestId,
                command.clientId(),
                command.redirectUri(),
                new ArrayList<>(scopes),
                command.state(),
                command.codeChallenge(),
                command.codeChallengeMethod(),
                tenantId,
                userId,
                Instant.now().plusSeconds(300)));
        return new AuthorizationView(
                authorizationRequestId, client.getClientCodeValue(), client.getClientName(), command.scope(), command.state());
    }

    @Transactional
    public AuthorizationDecisionResult decide(OAuth2DecisionCommand command) {
        OAuthAuthorizationRequest authorizationRequest = oAuthAuthorizationRepository
                .findById(command.authorizationRequestId())
                .filter(request -> !request.isUsed())
                .filter(request -> request.getExpireAt().isAfter(Instant.now()))
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.OAUTH_AUTHORIZATION_REQUEST_INVALID));
        if (!"APPROVE".equalsIgnoreCase(command.decision()) && !"REJECT".equalsIgnoreCase(command.decision())) {
            throw new AuthDomainException(AuthErrorCode.OAUTH_DECISION_INVALID);
        }
        authorizationRequest.markUsed();
        oAuthAuthorizationRepository.update(authorizationRequest);
        if ("REJECT".equalsIgnoreCase(command.decision())) {
            return new AuthorizationDecisionResult(
                    authorizationRequest.getRedirectUri() + "?error=access_denied&state="
                            + authorizationRequest.getState(),
                    null);
        }

        String authorizationCode = tokenCodec.randomToken();
        oAuthAuthorizationRepository.insertCode(authorizationCode, authorizationRequest);
        return new AuthorizationDecisionResult(
                authorizationRequest.getRedirectUri() + "?code=" + authorizationCode + "&state="
                        + authorizationRequest.getState(),
                authorizationCode);
    }

    @Transactional
    public OAuth2TokenDTO token(OAuth2TokenCommand command) {
        OAuthClient client = validateClient(command.clientId(), command.clientSecret());
        if ("authorization_code".equals(command.grantType())) {
            OAuthAuthorizationRequest request = oAuthAuthorizationRepository
                    .findByCode(command.code())
                    .orElseThrow(() -> new AuthDomainException(AuthErrorCode.OAUTH_AUTHORIZATION_CODE_INVALID));
            if (!request.getRedirectUri().equals(command.redirectUri())) {
                throw new AuthDomainException(AuthErrorCode.OAUTH_REDIRECT_URI_INVALID);
            }
            return issueOAuthTokens(
                    client,
                    request.getTenantIdValue(),
                    request.getUserId() == null ? null : request.getUserId().value(),
                    request.getScopes());
        }
        if ("refresh_token".equals(command.grantType())) {
            OAuthRefreshToken currentRefreshToken = oAuthAuthorizationRepository
                    .findByHash(tokenCodec.sha256(command.refreshToken()))
                    .filter(token -> "ACTIVE".equals(token.getTokenStatus()))
                    .orElseThrow(() -> new AuthDomainException(AuthErrorCode.OAUTH_REFRESH_TOKEN_INVALID));
            currentRefreshToken.markUsed();
            oAuthAuthorizationRepository.update(currentRefreshToken);
            Optional<OAuthAccessToken> accessToken =
                    oAuthAuthorizationRepository.findAccessByHash(currentRefreshToken.getAccessTokenId());
            Set<String> scopes = accessToken.map(OAuthAccessToken::getScopes).orElseGet(LinkedHashSet::new);
            return issueOAuthTokens(
                    client,
                    currentRefreshToken.getTenantIdValue(),
                    currentRefreshToken.getUserId() == null
                            ? null
                            : currentRefreshToken.getUserId().value(),
                    scopes);
        }
        throw new AuthDomainException(AuthErrorCode.OAUTH_GRANT_TYPE_UNSUPPORTED);
    }

    @Transactional
    public void revoke(OAuth2RevokeCommand command) {
        validateClient(command.clientId(), command.clientSecret());
        oAuthAuthorizationRepository
                .findAccessByHash(tokenCodec.sha256(command.token()))
                .ifPresent(accessToken -> {
                    accessToken.revoke();
                    oAuthAuthorizationRepository.update(accessToken);
                });
        oAuthAuthorizationRepository
                .findByHash(tokenCodec.sha256(command.token()))
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    oAuthAuthorizationRepository.update(refreshToken);
                });
    }

    private OAuth2TokenDTO issueOAuthTokens(OAuthClient client, Long tenantId, Long userId, Set<String> scopes) {
        Instant now = Instant.now();
        String accessTokenValue = tokenCodec.randomToken();
        String accessTokenId = tokenCodec.sha256(accessTokenValue);
        OAuthAccessToken accessToken = new OAuthAccessToken(
                accessTokenId,
                accessTokenId,
                client.getClientCodeValue(),
                tenantId,
                userId,
                new ArrayList<>(scopes),
                now,
                now.plusSeconds(client.getAccessTokenTtlSeconds()));
        oAuthAuthorizationRepository.update(accessToken);

        String refreshTokenValue = tokenCodec.randomToken();
        String refreshTokenHash = tokenCodec.sha256(refreshTokenValue);
        OAuthRefreshToken refreshToken = new OAuthRefreshToken(
                refreshTokenHash,
                refreshTokenHash,
                accessTokenId,
                client.getClientCodeValue(),
                tenantId,
                userId,
                now,
                now.plusSeconds(client.getRefreshTokenTtlSeconds()));
        oAuthAuthorizationRepository.update(refreshToken);

        return OAuth2AuthorizationAssembler.toOAuth2TokenDto(
                accessTokenValue,
                "Bearer",
                client.getAccessTokenTtlSeconds(),
                refreshTokenValue,
                String.join(" ", scopes));
    }

    private OAuthClient loadClient(String clientId) {
        return oAuthClientRepository
                .findByClientCode(clientId)
                .filter(OAuthClient::isEnabled)
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.OAUTH_CLIENT_INVALID));
    }

    private OAuthClient validateClient(String clientId, String clientSecret) {
        OAuthClient client = loadClient(clientId);
        String storedSecret = client.getClientSecret();
        boolean plainSecretMatches = storedSecret.equals(clientSecret);
        boolean hashedSecretMatches = passwordEncoder.matches(clientSecret, storedSecret);
        if (!plainSecretMatches && !hashedSecretMatches) {
            throw new AuthDomainException(AuthErrorCode.OAUTH_CLIENT_SECRET_INVALID);
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
