package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshDTO;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.application.support.TokenCodec;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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

    public UserTokenRefreshDTO refresh(String refreshToken) {
        RefreshTokenSession refreshTokenSession = authSessionRepository.findRefreshTokenByHash(tokenCodec.sha256(refreshToken))
                .filter(token -> "ACTIVE".equals(token.getTokenStatus()))
                .filter(token -> token.getExpireAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token invalid"));

        AuthSession authSession = authSessionRepository.findSessionBySessionId(refreshTokenSession.getSessionId())
                .filter(session -> "ACTIVE".equals(session.getSessionStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Session invalid"));

        // refresh token 采用一次性轮转：旧 token 先失效，再签发新的一对 token，降低长期凭证被重放的窗口。
        refreshTokenSession.markUsed(Instant.now());
        authSessionRepository.saveRefreshToken(refreshTokenSession);

        String newAccessToken = tokenCodec.issueUserAccessToken(authSession);
        String newRefreshToken = tokenCodec.randomToken();
        authSessionRepository.saveRefreshToken(new RefreshTokenSession(authSession.getSessionId(),
                tokenCodec.sha256(newRefreshToken), Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)));
        authAuditApplicationService.record("TOKEN_REFRESH", "SUCCESS", authSession.getSessionId());
        return new UserTokenRefreshDTO(newAccessToken, newRefreshToken, "Bearer",
                ACCESS_TOKEN_TTL_SECONDS, authSession.getSessionId());
    }

    public SessionValidationDTO verifyAccessToken(String accessToken) {
        Optional<String> sessionId = tokenCodec.parseSessionId(accessToken);
        if (sessionId.isEmpty()) {
            return new SessionValidationDTO(false, null, null, null, null, null, null);
        }
        // access token 校验最终还是回到会话仓储，确保已吊销或已过期会话即使 JWT 结构合法也不会被继续接受。
        return authSessionRepository.findSessionBySessionId(sessionId.get())
                .filter(session -> "ACTIVE".equals(session.getSessionStatus()))
                .filter(session -> session.getExpireAt().isAfter(Instant.now()))
                .map(session -> {
                    session.touch(Instant.now());
                    authSessionRepository.saveSession(session);
                    return new SessionValidationDTO(true, session.getTenantId(),
                            session.getUserId(),
                            session.getSessionId(), session.getIdentityId(), session.getIdentityType(),
                            session.getExpireAt());
                })
                .orElseGet(() -> new SessionValidationDTO(false, null, null, sessionId.get(), null, null, null));
    }

    public CurrentSessionDTO getSessionContext(String sessionId) {
        AuthSession authSession = authSessionRepository.findSessionBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        // 这里返回的是仓储里的当前会话快照，不重新解析 access token，避免出现 token 与服务端会话状态不一致。
        return new CurrentSessionDTO(authSession.getSessionId(), authSession.getTenantId(),
                authSession.getUserId(),
                authSession.getIdentityType(), authSession.getLoginType(), authSession.getSessionStatus(),
                authSession.getIssuedAt(), authSession.getLastAccessTime(), authSession.getExpireAt());
    }
}
