package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.application.support.AuthAuditApplicationService;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SessionApplicationServiceTest {

    @Test
    void shouldThrowBadRequestWhenAccessTokenIsInvalid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenApplicationService tokenApplicationService = mock(TokenApplicationService.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.parseSessionId("bad-token")).thenReturn(Optional.empty());
        SessionApplicationService service = new SessionApplicationService(
                authSessionRepository, tokenApplicationService, tokenCodec, authAuditApplicationService);

        assertThrows(BadRequestException.class, () -> service.currentSession("bad-token"));
    }

    @Test
    void shouldThrowNotFoundWhenSessionDoesNotExistDuringLogout() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenApplicationService tokenApplicationService = mock(TokenApplicationService.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.parseSessionId("token")).thenReturn(Optional.of("session-1"));
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.empty());
        SessionApplicationService service = new SessionApplicationService(
                authSessionRepository, tokenApplicationService, tokenCodec, authAuditApplicationService);

        assertThrows(NotFoundException.class, () -> service.logout("token"));
    }
}
