package com.github.thundax.bacon.auth.infra.repositoryimpl;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAuthRepository implements AuthSessionRepository, OAuthClientRepository, OAuthAuthorizationRepository {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, RefreshTokenSession> refreshTokenSessions = new ConcurrentHashMap<>();
    private final Map<String, OAuthClient> clients = new ConcurrentHashMap<>();
    private final Map<String, OAuthAuthorizationRequest> authorizationRequests = new ConcurrentHashMap<>();
    private final Map<String, OAuthAuthorizationRequest> authorizationCodes = new ConcurrentHashMap<>();
    private final Map<String, OAuthAccessToken> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, OAuthRefreshToken> oauthRefreshTokens = new ConcurrentHashMap<>();

    public InMemoryAuthRepository() {
        Instant now = Instant.now();
        OAuthClient demoClient = new OAuthClient(1L, "demo-client", "demo-secret", "Demo OAuth Client",
                "CONFIDENTIAL", Set.of("authorization_code", "refresh_token"), Set.of("openid", "profile"),
                Set.of("http://localhost:3000/callback"), 1800L, 2592000L, true,
                "dev@bacon.local", "demo", now, now);
        clients.put(demoClient.clientId(), demoClient);
    }

    @Override
    public AuthSession saveSession(AuthSession authSession) {
        sessions.put(authSession.getSessionId(), authSession);
        return authSession;
    }

    @Override
    public Optional<AuthSession> findSessionBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<AuthSession> findSessionsByTenantIdAndUserId(Long tenantId, Long userId) {
        return sessions.values().stream()
                .filter(session -> tenantId.equals(session.getTenantId()))
                .filter(session -> userId.equals(session.getUserId()))
                .toList();
    }

    @Override
    public List<AuthSession> findSessionsByTenantId(Long tenantId) {
        return sessions.values().stream()
                .filter(session -> tenantId.equals(session.getTenantId()))
                .toList();
    }

    @Override
    public RefreshTokenSession saveRefreshToken(RefreshTokenSession refreshTokenSession) {
        refreshTokenSessions.put(refreshTokenSession.getRefreshTokenHash(), refreshTokenSession);
        return refreshTokenSession;
    }

    @Override
    public Optional<RefreshTokenSession> findRefreshTokenByHash(String refreshTokenHash) {
        return Optional.ofNullable(refreshTokenSessions.get(refreshTokenHash));
    }

    @Override
    public void invalidateRefreshTokensBySessionId(String sessionId) {
        refreshTokenSessions.values().stream()
                .filter(token -> sessionId.equals(token.getSessionId()))
                .forEach(RefreshTokenSession::invalidate);
    }

    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @Override
    public OAuthAuthorizationRequest saveAuthorizationRequest(OAuthAuthorizationRequest authorizationRequest) {
        authorizationRequests.put(authorizationRequest.getAuthorizationRequestId(), authorizationRequest);
        return authorizationRequest;
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findAuthorizationRequestById(String authorizationRequestId) {
        return Optional.ofNullable(authorizationRequests.get(authorizationRequestId));
    }

    @Override
    public void saveAuthorizationCode(String authorizationCode, OAuthAuthorizationRequest authorizationRequest) {
        authorizationCodes.put(authorizationCode, authorizationRequest);
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findAuthorizationRequestByCode(String authorizationCode) {
        return Optional.ofNullable(authorizationCodes.get(authorizationCode));
    }

    @Override
    public OAuthAccessToken saveAccessToken(OAuthAccessToken accessToken) {
        accessTokens.put(accessToken.getTokenHash(), accessToken);
        return accessToken;
    }

    @Override
    public Optional<OAuthAccessToken> findAccessTokenByHash(String tokenHash) {
        return Optional.ofNullable(accessTokens.get(tokenHash));
    }

    @Override
    public OAuthRefreshToken saveOAuthRefreshToken(OAuthRefreshToken refreshToken) {
        oauthRefreshTokens.put(refreshToken.getTokenHash(), refreshToken);
        return refreshToken;
    }

    @Override
    public Optional<OAuthRefreshToken> findOAuthRefreshTokenByHash(String tokenHash) {
        return Optional.ofNullable(oauthRefreshTokens.get(tokenHash));
    }
}
