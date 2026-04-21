package com.github.thundax.bacon.auth.application.query;

import com.github.thundax.bacon.auth.application.assembler.TokenAssembler;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TokenQueryApplicationService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;

    public TokenQueryApplicationService(AuthSessionRepository authSessionRepository, TokenCodec tokenCodec) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
    }

    public SessionValidationDTO verifyAccessToken(TokenVerifyQuery query) {
        Optional<String> sessionId = tokenCodec.parseSessionId(query.accessToken());
        if (sessionId.isEmpty()) {
            return TokenAssembler.toSessionValidationDto(false, null, null, null, null, null, null);
        }
        return authSessionRepository
                .findBySessionId(sessionId.get())
                .filter(AuthSession::isActive)
                .filter(session -> session.getExpireAt().isAfter(Instant.now()))
                .map(session -> {
                    session.touch(Instant.now());
                    authSessionRepository.update(session);
                    return TokenAssembler.toSessionValidationDto(
                            true,
                            session.getTenantIdValue(),
                            session.getUserId() == null ? null : session.getUserId().value(),
                            session.getSessionId(),
                            session.getIdentityId(),
                            session.getIdentityType(),
                            session.getExpireAt());
                })
                .orElseGet(() -> TokenAssembler.toSessionValidationDto(
                        false, null, null, sessionId.get(), null, null, null));
    }

    public CurrentSessionDTO getSessionContext(SessionContextQuery query) {
        AuthSession authSession = authSessionRepository
                .findBySessionId(query.sessionId())
                .orElseThrow(() -> new NotFoundException("Session not found: " + query.sessionId()));
        return TokenAssembler.toCurrentSessionDto(
                authSession.getSessionId(),
                authSession.getTenantIdValue(),
                authSession.getUserId() == null ? null : authSession.getUserId().value(),
                authSession.getIdentityType(),
                authSession.getLoginType(),
                authSession.getStatusValue(),
                authSession.getIssuedAt(),
                authSession.getLastAccessTime(),
                authSession.getExpireAt());
    }
}
