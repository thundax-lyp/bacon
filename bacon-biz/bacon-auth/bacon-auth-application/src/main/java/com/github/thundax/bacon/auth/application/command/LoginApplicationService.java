package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.UserLoginDTO;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.application.support.LoginSecurityApplicationService;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.facade.UserCredentialReadFacade;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginApplicationService {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 1800L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800L;
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final AuthSessionRepository authSessionRepository;
    private final TokenCodec tokenCodec;
    private final AuthAuditApplicationService authAuditApplicationService;
    private final LoginSecurityApplicationService loginSecurityApplicationService;
    private final UserCredentialReadFacade userCredentialReadFacade;
    private final PasswordEncoder passwordEncoder;

    public LoginApplicationService(
            AuthSessionRepository authSessionRepository,
            TokenCodec tokenCodec,
            AuthAuditApplicationService authAuditApplicationService,
            LoginSecurityApplicationService loginSecurityApplicationService,
            UserCredentialReadFacade userCredentialReadFacade,
            PasswordEncoder passwordEncoder) {
        this.authSessionRepository = authSessionRepository;
        this.tokenCodec = tokenCodec;
        this.authAuditApplicationService = authAuditApplicationService;
        this.loginSecurityApplicationService = loginSecurityApplicationService;
        this.userCredentialReadFacade = userCredentialReadFacade;
        this.passwordEncoder = passwordEncoder;
    }

    public PasswordLoginChallengeResult issuePasswordLoginChallenge() {
        return loginSecurityApplicationService.issuePasswordLoginChallenge();
    }

    @Transactional
    public UserLoginDTO loginByPassword(PasswordLoginCommand command) {
        TenantId tenantId = normalizeTenantId(command.getTenantId());
        // 密码登录按“校验验证码 -> 解密密码 -> 查询凭据 -> 校验状态和口令”的顺序执行，
        // 这样既避免无意义的凭据查询，也保证明文密码只在内存里短暂存在。
        loginSecurityApplicationService.verifyPasswordCaptcha(command.getCaptchaKey(), command.getCaptchaCode());
        String plainPassword =
                loginSecurityApplicationService.decryptPassword(command.getRsaKeyId(), command.getPassword());
        UserCredentialFacadeResponse response = BaconContextHolder.callWithTenantId(
                tenantId.value(),
                () -> userCredentialReadFacade.getUserCredential(
                        new UserCredentialGetFacadeRequest("ACCOUNT", command.getAccount())));
        UserCredentialFacadeResponse validatedCredential = validatePasswordLoginCredential(response, plainPassword);
        return createLoginSession(
                tenantId.value(),
                validatedCredential.userId(),
                validatedCredential.identityId(),
                validatedCredential.identityType(),
                "PASSWORD",
                validatedCredential.needChangePassword());
    }

    @Transactional
    public UserLoginDTO loginBySms(String phone, String smsCaptcha) {
        return createLoginSession(1001L, 2002L, 3002L, "PHONE", "SMS", null);
    }

    @Transactional
    public UserLoginDTO loginByWecom(String code) {
        return createLoginSession(1001L, 2003L, 3003L, "WECOM", "WECOM", null);
    }

    @Transactional
    public UserLoginDTO loginByGithub(String code) {
        return createLoginSession(1001L, 2004L, 3004L, "GITHUB", "GITHUB", null);
    }

    private UserCredentialFacadeResponse validatePasswordLoginCredential(
            UserCredentialFacadeResponse credential, String plainPassword) {
        if (credential == null) {
            throw new BadRequestException("Invalid account or password");
        }
        if (!ACTIVE_STATUS.equals(credential.identityStatus())) {
            throw new BadRequestException("Current account is disabled");
        }
        if (!ACTIVE_STATUS.equals(credential.status())) {
            throw new BadRequestException("Current user is not enabled");
        }
        if (!ACTIVE_STATUS.equals(credential.credentialStatus())) {
            throw new BadRequestException("Current credential is not active");
        }
        if (credential.credentialExpiresAt() != null
                && credential.credentialExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Current credential has expired");
        }
        if (!passwordEncoder.matches(plainPassword, credential.passwordHash())) {
            throw new BadRequestException("Invalid account or password");
        }
        return credential;
    }

    private UserLoginDTO createLoginSession(
            Long tenantId,
            Long userId,
            Long identityId,
            String identityType,
            String loginType,
            Boolean needChangePassword) {
        Instant now = Instant.now();
        String sessionId = UUID.randomUUID().toString();
        // 会话和 refresh token 分开存储：会话承载当前登录上下文，refresh token 只负责后续换新 access token。
        AuthSession authSession = new AuthSession(
                idGenerator.getAndIncrement(),
                sessionId,
                tenantId,
                userId,
                identityId,
                identityType,
                loginType,
                now,
                now.plus(ACCESS_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS));
        authSessionRepository.update(authSession);

        String accessToken = tokenCodec.issueUserAccessToken(authSession);
        String refreshToken = tokenCodec.randomToken();
        // refresh token 入库只存哈希，不保留明文，防止仓储泄露时直接复用长期凭证。
        authSessionRepository.update(new RefreshTokenSession(
                sessionId,
                tokenCodec.sha256(refreshToken),
                now,
                now.plus(REFRESH_TOKEN_TTL_SECONDS, ChronoUnit.SECONDS)));

        authAuditApplicationService.record("LOGIN_" + loginType, "SUCCESS", sessionId);
        return new UserLoginDTO(
                accessToken,
                refreshToken,
                "Bearer",
                ACCESS_TOKEN_TTL_SECONDS,
                sessionId,
                userId,
                tenantId,
                needChangePassword);
    }

    private TenantId normalizeTenantId(Long tenantId) {
        if (tenantId == null) {
            throw new BadRequestException("tenantId must not be null");
        }
        return TenantId.of(tenantId);
    }
}
