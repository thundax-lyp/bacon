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

class TokenApplicationServiceTest {

    @Test
    void shouldThrowBadRequestWhenRefreshTokenIsInvalid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(tokenCodec.sha256("refresh-token")).thenReturn("hashed-refresh-token");
        when(authSessionRepository.findByHash("hashed-refresh-token")).thenReturn(Optional.empty());
        TokenApplicationService service =
                new TokenApplicationService(authSessionRepository, tokenCodec, authAuditApplicationService);

        assertThrows(BadRequestException.class, () -> service.refresh("refresh-token"));
    }

    @Test
    void shouldThrowNotFoundWhenSessionContextDoesNotExist() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        AuthAuditApplicationService authAuditApplicationService = mock(AuthAuditApplicationService.class);
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.empty());
        TokenApplicationService service =
                new TokenApplicationService(authSessionRepository, tokenCodec, authAuditApplicationService);

        assertThrows(NotFoundException.class, () -> service.getSessionContext("session-1"));
    }
}
