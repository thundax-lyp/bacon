package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionApplicationService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenApplicationService tokenApplicationService;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public SessionApplicationService(
            AuthSessionRepository authSessionRepository,
            TokenApplicationService tokenApplicationService,
            TokenCodec tokenCodec,
            AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenApplicationService = tokenApplicationService;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    public CurrentSessionDTO currentSession(String accessToken) {
        String sessionId = tokenCodec
                .parseSessionId(accessToken)
                .orElseThrow(() -> new BadRequestException("Access token invalid"));
        return tokenApplicationService.getSessionContext(sessionId);
    }

    @Transactional
    public void logout(String accessToken) {
        String sessionId = tokenCodec
                .parseSessionId(accessToken)
                .orElseThrow(() -> new BadRequestException("Access token invalid"));
        AuthSession authSession = authSessionRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        authSession.logout(Instant.now());
        authSessionRepository.update(authSession);
        authSessionRepository.markInvalidBySessionId(sessionId);
        authAuditApplicationService.record("LOGOUT", "SUCCESS", sessionId);
    }

    @Transactional
    public void invalidateUserSessions(Long tenantId, Long userId, String reason) {
        List<AuthSession> sessions = authSessionRepository.listByTenantIdAndUserId(tenantId, userId);
        sessions.forEach(session -> {
            session.invalidate(reason);
            authSessionRepository.update(session);
            authSessionRepository.markInvalidBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record("INVALIDATE_USER_SESSIONS", "SUCCESS", tenantId + ":" + userId);
    }

    @Transactional
    public void invalidateTenantSessions(Long tenantId, String reason) {
        List<AuthSession> sessions = authSessionRepository.listByTenantId(tenantId);
        sessions.forEach(session -> {
            session.invalidate(reason);
            authSessionRepository.update(session);
            authSessionRepository.markInvalidBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record("INVALIDATE_TENANT_SESSIONS", "SUCCESS", String.valueOf(tenantId));
    }

    @Transactional
    public void invalidateSession(String sessionId, String reason) {
        AuthSession authSession = authSessionRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        authSession.invalidate(reason);
        authSessionRepository.update(authSession);
        authSessionRepository.markInvalidBySessionId(sessionId);
        authAuditApplicationService.record("INVALIDATE_SESSION", "SUCCESS", sessionId);
    }
}
