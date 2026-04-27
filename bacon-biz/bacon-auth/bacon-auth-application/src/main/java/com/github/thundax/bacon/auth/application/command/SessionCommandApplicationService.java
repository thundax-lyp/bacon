package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionCommandApplicationService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public SessionCommandApplicationService(
            AuthSessionRepository authSessionRepository,
            TokenCodec tokenCodec,
            AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    @Transactional
    public void logout(SessionLogoutCommand command) {
        String sessionId = tokenCodec
                .parseSessionId(command.accessToken())
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.INVALID_ACCESS_TOKEN));
        AuthSession authSession = authSessionRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.SESSION_NOT_FOUND, sessionId));
        authSession.logout(Instant.now());
        authSessionRepository.update(authSession);
        authSessionRepository.markInvalidBySessionId(sessionId);
        authAuditApplicationService.record("LOGOUT", "SUCCESS", sessionId);
    }

    @Transactional
    public void invalidateUserSessions(SessionInvalidateUserCommand command) {
        List<AuthSession> sessions = authSessionRepository.listByTenantIdAndUserId(command.tenantId(), command.userId());
        sessions.forEach(session -> {
            session.invalidate(command.reason());
            authSessionRepository.update(session);
            authSessionRepository.markInvalidBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record(
                "INVALIDATE_USER_SESSIONS", "SUCCESS", command.tenantId() + ":" + command.userId());
    }

    @Transactional
    public void invalidateTenantSessions(SessionInvalidateTenantCommand command) {
        List<AuthSession> sessions = authSessionRepository.listByTenantId(command.tenantId());
        sessions.forEach(session -> {
            session.invalidate(command.reason());
            authSessionRepository.update(session);
            authSessionRepository.markInvalidBySessionId(session.getSessionId());
        });
        authAuditApplicationService.record("INVALIDATE_TENANT_SESSIONS", "SUCCESS", String.valueOf(command.tenantId()));
    }

    @Transactional
    public void invalidateSession(SessionInvalidateCommand command) {
        AuthSession authSession = authSessionRepository
                .findBySessionId(command.sessionId())
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.SESSION_NOT_FOUND, command.sessionId()));
        authSession.invalidate(command.reason());
        authSessionRepository.update(authSession);
        authSessionRepository.markInvalidBySessionId(command.sessionId());
        authAuditApplicationService.record("INVALIDATE_SESSION", "SUCCESS", command.sessionId());
    }
}
