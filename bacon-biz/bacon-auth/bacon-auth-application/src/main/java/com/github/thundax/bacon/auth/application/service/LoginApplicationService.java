package com.github.thundax.bacon.auth.application.service;

import com.github.thundax.bacon.auth.api.dto.UserLoginDTO;
import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoginApplicationService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L;

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;
    private final LoginSecurityApplicationService loginSecurityApplicationService;
    private final UserReadFacade userReadFacade;
    private final PasswordEncoder passwordEncoder;

    public LoginApplicationService(AuthSessionRepository authSessionRepository, TokenCodec tokenCodec,
                                   AuthAuditApplicationService authAuditApplicationService,
                                   LoginSecurityApplicationService loginSecurityApplicationService,
                                   UserReadFacade userReadFacade,
                                   PasswordEncoder passwordEncoder) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
        this.loginSecurityApplicationService = loginSecurityApplicationService;
        this.userReadFacade = userReadFacade;
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordLoginChallengeResult issuePasswordLoginChallenge() {
        return loginSecurityApplicationService.issuePasswordLoginChallenge();
    }

    public UserLoginDTO loginByPassword(PasswordLoginCommand command) {
        Long tenantId = command.getTenantId() == null ? 1001L : command.getTenantId();
        loginSecurityApplicationService.verifyPasswordCaptcha(command.getCaptchaKey(), command.getCaptchaCode());
        String plainPassword = loginSecurityApplicationService.decryptPassword(command.getRsaKeyId(), command.getPassword());
        UserLoginCredentialDTO credential = userReadFacade.getUserLoginCredential(tenantId, "ACCOUNT", command.getAccount());
        validatePasswordLoginCredential(credential, plainPassword);
        return createLoginSession(credential.getTenantId(), credential.getUserId(), credential.getIdentityValue(),
                credential.getIdentityType(), "PASSWORD", false);
    }

    public UserLoginDTO loginBySms(String phone, String smsCaptcha) {
        return createLoginSession(1001L, 2002L, phone, "PHONE", "SMS", null);
    }

    public UserLoginDTO loginByWecom(String code) {
        return createLoginSession(1001L, 2003L, code, "WECOM", "WECOM", null);
    }

    public UserLoginDTO loginByGithub(String code) {
        return createLoginSession(1001L, 2004L, code, "GITHUB", "GITHUB", null);
    }

    private void validatePasswordLoginCredential(UserLoginCredentialDTO credential, String plainPassword) {
        if (credential == null) {
            throw new BadRequestException("Invalid account or password");
        }
        if (!credential.isIdentityEnabled()) {
            throw new BadRequestException("Current account is disabled");
        }
        if (credential.isDeleted()) {
            throw new BadRequestException("Current account has been deleted");
        }
        if (!"ENABLED".equalsIgnoreCase(credential.getStatus())) {
            throw new BadRequestException("Current user is not enabled");
        }
        if (!passwordEncoder.matches(plainPassword, credential.getPasswordHash())) {
            throw new BadRequestException("Invalid account or password");
        }
    }

    private UserLoginDTO createLoginSession(Long tenantId, Long userId, String identitySeed, String identityType,
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
        return new UserLoginDTO(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_TTL_SECONDS, sessionId,
                userId, tenantId, needChangePassword);
    }
}
