package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.application.support.TokenCodec;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SessionApplicationService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenApplicationService tokenApplicationService;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public SessionApplicationService(AuthSessionRepository authSessionRepository,
                                     TokenApplicationService tokenApplicationService, TokenCodec tokenCodec,
                                     AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenApplicationService = tokenApplicationService;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    public CurrentSessionDTO currentSession(String accessToken) {
        String sessionId = tokenCodec.parseSessionId(accessToken)
                .orElseThrow(() -> new IllegalArgumentException("Access token invalid"));
        return tokenApplicationService.getSessionContext(sessionId);
    }

    public void logout(String accessToken) {
        String sessionId = tokenCodec.parseSessionId(accessToken)
                .orElseThrow(() -> new IllegalArgumentException("Access token invalid"));
        AuthSession authSession = authSessionRepository.findSessionBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        authSession.logout(Instant.now());
        authSessionRepository.invalidateRefreshTokensBySessionId(sessionId);
        authAuditApplicationService.record("LOGOUT", "SUCCESS", sessionId);
    }

    public void invalidateUserSessions(Long tenantId, Long userId, String reason) {
        List<AuthSession> sessions = authSessionRepository.findSessionsByTenantIdAndUserId(tenantId, userId);
        sessions.forEach(session -> {
            session.invalidate(reason);
            authSessionRepository.invalidateRefreshTokensBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record("INVALIDATE_USER_SESSIONS", "SUCCESS", tenantId + ":" + userId);
    }

    public void invalidateTenantSessions(Long tenantId, String reason) {
        List<AuthSession> sessions = authSessionRepository.findSessionsByTenantId(tenantId);
        sessions.forEach(session -> {
            session.invalidate(reason);
            authSessionRepository.invalidateRefreshTokensBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record("INVALIDATE_TENANT_SESSIONS", "SUCCESS", String.valueOf(tenantId));
    }

    public void invalidateSession(String sessionId, String reason) {
        AuthSession authSession = authSessionRepository.findSessionBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        authSession.invalidate(reason);
        authSessionRepository.invalidateRefreshTokensBySessionId(sessionId);
        authAuditApplicationService.record("INVALIDATE_SESSION", "SUCCESS", sessionId);
    }
}
