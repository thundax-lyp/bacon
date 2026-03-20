package com.github.thundax.bacon.auth.application.service;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshResponse;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TokenApplicationService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L;

    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public TokenApplicationService(AuthSessionRepository authSessionRepository, TokenCodec tokenCodec,
                                   AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    public UserTokenRefreshResponse refresh(String refreshToken) {
        RefreshTokenSession refreshTokenSession = authSessionRepository.findRefreshTokenByHash(tokenCodec.sha256(refreshToken))
                .filter(token -> "ACTIVE".equals(token.getTokenStatus()))
                .filter(token -> token.getExpireAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token invalid"));

        AuthSession authSession = authSessionRepository.findSessionBySessionId(refreshTokenSession.getSessionId())
                .filter(session -> "ACTIVE".equals(session.getSessionStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Session invalid"));

        refreshTokenSession.markUsed(Instant.now());

        String newAccessToken = tokenCodec.issueUserAccessToken(authSession);
        String newRefreshToken = tokenCodec.randomToken();
        authSessionRepository.saveRefreshToken(new RefreshTokenSession(authSession.getSessionId(),
                tokenCodec.sha256(newRefreshToken), Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)));
        authAuditApplicationService.record("TOKEN_REFRESH", "SUCCESS", authSession.getSessionId());
        return new UserTokenRefreshResponse(newAccessToken, newRefreshToken, "Bearer",
                ACCESS_TOKEN_TTL_SECONDS, authSession.getSessionId());
    }

    public SessionValidationResponse verifyAccessToken(String accessToken) {
        Optional<String> sessionId = tokenCodec.parseSessionId(accessToken);
        if (sessionId.isEmpty()) {
            return new SessionValidationResponse(false, null, null, null, null, null, null);
        }
        return authSessionRepository.findSessionBySessionId(sessionId.get())
                .filter(session -> "ACTIVE".equals(session.getSessionStatus()))
                .filter(session -> session.getExpireAt().isAfter(Instant.now()))
                .map(session -> {
                    session.touch(Instant.now());
                    return new SessionValidationResponse(true, session.getTenantId(), session.getUserId(),
                            session.getSessionId(), session.getIdentityId(), session.getIdentityType(),
                            session.getExpireAt());
                })
                .orElseGet(() -> new SessionValidationResponse(false, null, null, sessionId.get(), null, null, null));
    }

    public CurrentSessionResponse getSessionContext(String sessionId) {
        AuthSession authSession = authSessionRepository.findSessionBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return new CurrentSessionResponse(authSession.getSessionId(), authSession.getTenantId(), authSession.getUserId(),
                authSession.getIdentityType(), authSession.getLoginType(), authSession.getSessionStatus(),
                authSession.getIssuedAt(), authSession.getLastAccessTime(), authSession.getExpireAt());
    }
}
