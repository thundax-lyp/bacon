package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.assembler.TokenAssembler;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.UserTokenRefreshDTO;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenCommandApplicationService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L;

    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public TokenCommandApplicationService(
            AuthSessionRepository authSessionRepository,
            TokenCodec tokenCodec,
            AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    @Transactional
    public UserTokenRefreshDTO refresh(TokenRefreshCommand command) {
        RefreshTokenSession refreshTokenSession = authSessionRepository
                .findByHash(tokenCodec.sha256(command.refreshToken()))
                .filter(RefreshTokenSession::isActive)
                .filter(token -> token.getExpireAt().isAfter(Instant.now()))
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        AuthSession authSession = authSessionRepository
                .findBySessionId(refreshTokenSession.getSessionIdValue())
                .filter(AuthSession::isActive)
                .orElseThrow(() -> new AuthDomainException(AuthErrorCode.INVALID_SESSION));

        refreshTokenSession.markUsed(Instant.now());
        authSessionRepository.update(refreshTokenSession);

        String newAccessToken = tokenCodec.issueUserAccessToken(authSession);
        String newRefreshToken = tokenCodec.randomToken();
        authSessionRepository.update(new RefreshTokenSession(
                authSession.getSessionId(),
                tokenCodec.sha256(newRefreshToken),
                Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)));
        authAuditApplicationService.record("TOKEN_REFRESH", "SUCCESS", authSession.getSessionId());
        return TokenAssembler.toUserTokenRefreshDto(
                newAccessToken, newRefreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS, authSession.getSessionId());
    }
}
