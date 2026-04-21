package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.dto.UserTokenRefreshDTO;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TokenCommandApplicationServiceTest {

    @Test
    void shouldThrowBadRequestWhenRefreshTokenIsInvalid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.sha256("refresh-token")).thenReturn("hashed-refresh-token");
        when(authSessionRepository.findByHash("hashed-refresh-token")).thenReturn(Optional.empty());
        TokenCommandApplicationService service =
                new TokenCommandApplicationService(authSessionRepository, tokenCodec, authAuditApplicationService);

        assertThrows(BadRequestException.class, () -> service.refresh(new TokenRefreshCommand("refresh-token")));
    }

    @Test
    void shouldRefreshTokenAndRotateRefreshTokenWhenCommandIsValid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        doNothing().when(authAuditApplicationService).record("TOKEN_REFRESH", "SUCCESS", "session-1");

        RefreshTokenSession refreshTokenSession = new RefreshTokenSession(
                "session-1",
                "hashed-refresh-token",
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(600));
        AuthSession authSession = new AuthSession(
                1L,
                "session-1",
                1001L,
                2001L,
                3001L,
                "ACCOUNT",
                "PASSWORD",
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(600));

        when(tokenCodec.sha256("refresh-token")).thenReturn("hashed-refresh-token");
        when(authSessionRepository.findByHash("hashed-refresh-token")).thenReturn(Optional.of(refreshTokenSession));
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.of(authSession));
        when(authSessionRepository.update(any(RefreshTokenSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, RefreshTokenSession.class));
        when(tokenCodec.issueUserAccessToken(authSession)).thenReturn("access-token");
        when(tokenCodec.randomToken()).thenReturn("new-refresh-token");
        when(tokenCodec.sha256("new-refresh-token")).thenReturn("hashed-new-refresh-token");

        TokenCommandApplicationService service =
                new TokenCommandApplicationService(authSessionRepository, tokenCodec, authAuditApplicationService);

        UserTokenRefreshDTO result = service.refresh(new TokenRefreshCommand("refresh-token"));

        assertEquals("access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(1800L, result.getExpiresIn());
        assertEquals("session-1", result.getSessionId());
        verify(authSessionRepository, times(2)).update(any(RefreshTokenSession.class));
        verify(authAuditApplicationService).record("TOKEN_REFRESH", "SUCCESS", "session-1");
    }
}
