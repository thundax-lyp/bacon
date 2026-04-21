package com.github.thundax.bacon.auth.application.query;

import com.github.thundax.bacon.auth.application.assembler.TokenAssembler;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SessionQueryApplicationService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;

    public SessionQueryApplicationService(AuthSessionRepository authSessionRepository, TokenCodec tokenCodec) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
    }

    public CurrentSessionDTO currentSession(SessionCurrentQuery query) {
        String sessionId = tokenCodec
                .parseSessionId(query.accessToken())
                .orElseThrow(() -> new BadRequestException("Access token invalid"));
        return getSessionContext(new SessionContextQuery(sessionId));
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
