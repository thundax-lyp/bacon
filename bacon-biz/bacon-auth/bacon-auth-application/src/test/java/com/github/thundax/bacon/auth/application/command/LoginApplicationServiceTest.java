package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.dto.UserLoginDTO;
import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.application.support.LoginSecurityApplicationService;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.upms.api.facade.UserCredentialReadFacade;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class LoginApplicationServiceTest {

    @AfterEach
    void tearDown() {
        com.github.thundax.bacon.common.core.context.BaconContextHolder.clear();
    }

    @Test
    void shouldCreatePasswordLoginSessionWhenCredentialIsValid() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        when(authSessionRepository.update(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthSession.class));
        when(authSessionRepository.update(any(RefreshTokenSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, RefreshTokenSession.class));

        TokenCodec tokenCodec = mock(TokenCodec.class);
        when(tokenCodec.issueUserAccessToken(any(AuthSession.class))).thenReturn("access-token");
        when(tokenCodec.randomToken()).thenReturn("refresh-token");
        when(tokenCodec.sha256("refresh-token")).thenReturn("refresh-token-hash");

        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        doNothing().when(authAuditApplicationService).record(eq("LOGIN_PASSWORD"), eq("SUCCESS"), any(String.class));

        LoginSecurityApplicationService loginSecurityApplicationService = mock(LoginSecurityApplicationService.class);
        doNothing().when(loginSecurityApplicationService).verifyPasswordCaptcha("captcha-key", "captcha-code");
        when(loginSecurityApplicationService.decryptPassword("rsa-key", "cipher-password")).thenReturn("plain-password");

        UserCredentialReadFacade userCredentialReadFacade = mock(UserCredentialReadFacade.class);
        when(userCredentialReadFacade.getUserCredential(any())).thenReturn(UserCredentialFacadeResponse.from(
                2001L,
                3001L,
                "demo",
                null,
                "ACCOUNT",
                "demo",
                "ACTIVE",
                4001L,
                "PASSWORD",
                "ACTIVE",
                true,
                Instant.now().plusSeconds(600),
                null,
                false,
                List.of(),
                "ENABLED",
                passwordEncoder.encode("plain-password")));

        LoginApplicationService service = new LoginApplicationService(
                authSessionRepository,
                tokenCodec,
                authAuditApplicationService,
                loginSecurityApplicationService,
                userCredentialReadFacade,
                passwordEncoder);

        UserLoginDTO result = service.loginByPassword(
                new PasswordLoginCommand(1001L, "demo", "cipher-password", "rsa-key", "captcha-key", "captcha-code"));

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals(2001L, result.getUserId());
        assertEquals(1001L, result.getTenantId());
        assertEquals(Boolean.TRUE, result.getNeedChangePassword());
    }
}
