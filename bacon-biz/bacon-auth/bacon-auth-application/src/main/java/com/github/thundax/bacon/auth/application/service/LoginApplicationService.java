package com.github.thundax.bacon.auth.application.service;

import com.github.thundax.bacon.auth.api.dto.UserLoginResponse;
import com.github.thundax.bacon.auth.domain.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class LoginApplicationService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L;

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;

    public LoginApplicationService(AuthSessionRepository authSessionRepository, TokenCodec tokenCodec,
                                   AuthAuditApplicationService authAuditApplicationService) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
    }

    public UserLoginResponse loginByPassword(String account, String password) {
        return createLoginSession(1001L, 2001L, account, "ACCOUNT", "PASSWORD", false);
    }

    public UserLoginResponse loginBySms(String phone, String smsCaptcha) {
        return createLoginSession(1001L, 2002L, phone, "PHONE", "SMS", null);
    }

    public UserLoginResponse loginByWecom(String code) {
        return createLoginSession(1001L, 2003L, code, "WECOM", "WECOM", null);
    }

    public UserLoginResponse loginByGithub(String code) {
        return createLoginSession(1001L, 2004L, code, "GITHUB", "GITHUB", null);
    }

    private UserLoginResponse createLoginSession(Long tenantId, Long userId, String identitySeed, String identityType,
                                                 String loginType, Boolean needChangePassword) {
        Instant now = Instant.now();
        String sessionId = UUID.randomUUID().toString();
        AuthSession authSession = new AuthSession(idGenerator.getAndIncrement(), sessionId, tenantId, userId,
                identityType + ":" + identitySeed, identityType, loginType, now, now.plus(ACCESS_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS));
        authSessionRepository.saveSession(authSession);

        String accessToken = tokenCodec.issueUserAccessToken(authSession);
        String refreshToken = tokenCodec.randomToken();
        authSessionRepository.saveRefreshToken(
                new RefreshTokenSession(sessionId, tokenCodec.sha256(refreshToken), now,
                        now.plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)));

        authAuditApplicationService.record("LOGIN_" + loginType, "SUCCESS", sessionId);
        return new UserLoginResponse(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS, sessionId,
                userId, tenantId, needChangePassword);
    }
}
