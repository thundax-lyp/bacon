package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.api.dto.OAuth2IntrospectionDTO;
import com.github.thundax.bacon.auth.api.dto.OAuth2TokenDTO;
import com.github.thundax.bacon.auth.api.dto.OAuth2UserinfoDTO;
import com.github.thundax.bacon.auth.application.support.TokenCodec;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class OAuth2AuthorizationApplicationService {

    private final OAuthClientRepository oAuthClientRepository;
    private final OAuthAuthorizationRepository oAuthAuthorizationRepository;
    private final SessionApplicationService sessionApplicationService;
    private final TokenCodec tokenCodec;
    private final PasswordEncoder passwordEncoder;

    public OAuth2AuthorizationApplicationService(OAuthClientRepository oAuthClientRepository,
                                                 OAuthAuthorizationRepository oAuthAuthorizationRepository,
                                                 SessionApplicationService sessionApplicationService,
                                                 TokenCodec tokenCodec,
                                                 PasswordEncoder passwordEncoder) {
        this.oAuthClientRepository = oAuthClientRepository;
        this.oAuthAuthorizationRepository = oAuthAuthorizationRepository;
        this.sessionApplicationService = sessionApplicationService;
        this.tokenCodec = tokenCodec;
        this.passwordEncoder = passwordEncoder;
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
        String tenantNo = currentSession.getTenantNo();
        String userId = currentSession.getUserId();

        // authorize 阶段只落授权请求，不直接发 code；真正的授权决定由后续 approve/reject 明确给出。
        String authorizationRequestId = UUID.randomUUID().toString();
        oAuthAuthorizationRepository.saveAuthorizationRequest(new OAuthAuthorizationRequest(
                authorizationRequestId, clientId, redirectUri, scopes, state, codeChallenge, codeChallengeMethod,
                tenantNo, userId, Instant.now().plusSeconds(300)));
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
        oAuthAuthorizationRepository.saveAuthorizationRequest(authorizationRequest);
        if ("REJECT".equalsIgnoreCase(decision)) {
            return new AuthorizationDecisionResult(authorizationRequest.getRedirectUri()
                    + "?error=access_denied&state=" + authorizationRequest.getState(), null);
        }

        // 授权请求一旦 APPROVE 就立即标记已使用，避免同一个 request 被重复换出多个 authorization code。
        String authorizationCode = tokenCodec.randomToken();
        oAuthAuthorizationRepository.saveAuthorizationCode(authorizationCode, authorizationRequest);
        return new AuthorizationDecisionResult(authorizationRequest.getRedirectUri()
                + "?code=" + authorizationCode + "&state=" + authorizationRequest.getState(), authorizationCode);
    }

    public OAuth2TokenDTO token(String grantType, String code, String redirectUri, String clientId,
                                     String clientSecret, String codeVerifier, String refreshToken) {
        OAuthClient client = validateClient(clientId, clientSecret);
        if ("authorization_code".equals(grantType)) {
            // authorization_code 交换时复用授权请求里固化的 tenant/user/scopes，避免由客户端自行提交这些敏感上下文。
            OAuthAuthorizationRequest request = oAuthAuthorizationRepository.findAuthorizationRequestByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Authorization code invalid"));
            if (!request.getRedirectUri().equals(redirectUri)) {
                throw new IllegalArgumentException("Redirect uri invalid");
            }
            return issueOAuthTokens(client, request.getTenantNo(), request.getUserId(), request.getScopes());
        }
        if ("refresh_token".equals(grantType)) {
            OAuthRefreshToken currentRefreshToken = oAuthAuthorizationRepository.findOAuthRefreshTokenByHash(tokenCodec.sha256(refreshToken))
                    .filter(token -> "ACTIVE".equals(token.getTokenStatus()))
                    .orElseThrow(() -> new IllegalArgumentException("OAuth refresh token invalid"));
            // refresh_token 换新采用轮转模式：旧 refresh token 标记 USED，再签发新的 access/refresh 对。
            currentRefreshToken.markUsed();
            oAuthAuthorizationRepository.saveOAuthRefreshToken(currentRefreshToken);
            Optional<OAuthAccessToken> accessToken = oAuthAuthorizationRepository.findAccessTokenByHash(currentRefreshToken.getAccessTokenId());
            Set<String> scopes = accessToken.map(OAuthAccessToken::getScopes).orElseGet(LinkedHashSet::new);
            return issueOAuthTokens(client, currentRefreshToken.getTenantNo(), currentRefreshToken.getUserId(), scopes);
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
                        accessToken.getTenantNo(), accessToken.getExpireAt().getEpochSecond()))
                .orElse(new OAuth2IntrospectionDTO(false, clientId, "", "", "", 0L));
    }

    public void revoke(String token, String clientId, String clientSecret) {
        validateClient(clientId, clientSecret);
        oAuthAuthorizationRepository.findAccessTokenByHash(tokenCodec.sha256(token)).ifPresent(accessToken -> {
            accessToken.revoke();
            oAuthAuthorizationRepository.saveAccessToken(accessToken);
        });
        oAuthAuthorizationRepository.findOAuthRefreshTokenByHash(tokenCodec.sha256(token)).ifPresent(refreshToken -> {
            refreshToken.revoke();
            oAuthAuthorizationRepository.saveOAuthRefreshToken(refreshToken);
        });
    }

    public OAuth2UserinfoDTO userinfo(String accessToken) {
        OAuthAccessToken token = oAuthAuthorizationRepository.findAccessTokenByHash(tokenCodec.sha256(accessToken))
                .filter(current -> "ACTIVE".equals(current.getTokenStatus()))
                .orElseThrow(() -> new IllegalArgumentException("OAuth access token invalid"));
        String name = token.getScopes().contains("profile") ? "demo-user-" + token.getUserId() : null;
        return new OAuth2UserinfoDTO(token.getUserId(), token.getTenantNo(), name);
    }

    private OAuth2TokenDTO issueOAuthTokens(OAuthClient client, String tenantNo, String userId, Set<String> scopes) {
        Instant now = Instant.now();
        String accessTokenValue = tokenCodec.randomToken();
        String accessTokenId = tokenCodec.sha256(accessTokenValue);
        // access token 和 refresh token 在仓储层都使用哈希作为主识别键，避免明文 token 被直接持久化。
        OAuthAccessToken accessToken = new OAuthAccessToken(accessTokenId, accessTokenId, client.getClientId(), tenantNo,
                userId, scopes, now, now.plusSeconds(client.getAccessTokenTtlSeconds()));
        oAuthAuthorizationRepository.saveAccessToken(accessToken);

        String refreshTokenValue = tokenCodec.randomToken();
        String refreshTokenHash = tokenCodec.sha256(refreshTokenValue);
        OAuthRefreshToken refreshToken = new OAuthRefreshToken(refreshTokenHash, refreshTokenHash, accessTokenId,
                client.getClientId(), tenantNo, userId, now, now.plusSeconds(client.getRefreshTokenTtlSeconds()));
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
        String storedSecret = client.getClientSecret();
        boolean plainSecretMatches = storedSecret.equals(clientSecret);
        boolean hashedSecretMatches = passwordEncoder.matches(clientSecret, storedSecret);
        if (!plainSecretMatches && !hashedSecretMatches) {
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
