package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SessionCommandApplicationServiceTest {

    @Test
    void shouldThrowBadRequestWhenAccessTokenIsInvalid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.parseSessionId("bad-token")).thenReturn(Optional.empty());
        SessionCommandApplicationService service = new SessionCommandApplicationService(
                authSessionRepository, tokenCodec, authAuditApplicationService);

        assertThrows(BadRequestException.class, () -> service.logout(new SessionLogoutCommand("bad-token")));
    }

    @Test
    void shouldThrowNotFoundWhenSessionDoesNotExistDuringLogout() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.parseSessionId("token")).thenReturn(Optional.of("session-1"));
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.empty());
        SessionCommandApplicationService service = new SessionCommandApplicationService(
                authSessionRepository, tokenCodec, authAuditApplicationService);

        assertThrows(NotFoundException.class, () -> service.logout(new SessionLogoutCommand("token")));
    }

    @Test
    void shouldLogoutSessionAndMarkItInvalidWhenCommandIsValid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
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
        when(tokenCodec.parseSessionId("token")).thenReturn(Optional.of("session-1"));
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.of(authSession));
        when(authSessionRepository.update(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthSession.class));
        SessionCommandApplicationService service = new SessionCommandApplicationService(
                authSessionRepository, tokenCodec, authAuditApplicationService);

        assertDoesNotThrow(() -> service.logout(new SessionLogoutCommand("token")));

        verify(authSessionRepository).update(any(AuthSession.class));
        verify(authSessionRepository).markInvalidBySessionId("session-1");
        verify(authAuditApplicationService).record("LOGOUT", "SUCCESS", "session-1");
    }
}
