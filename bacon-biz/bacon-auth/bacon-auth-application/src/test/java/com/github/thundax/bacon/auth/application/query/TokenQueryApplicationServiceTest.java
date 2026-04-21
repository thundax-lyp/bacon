package com.github.thundax.bacon.auth.application.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TokenQueryApplicationServiceTest {

    @Test
    void shouldReturnFalseWhenAccessTokenIsInvalid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        when(tokenCodec.parseSessionId("bad-token")).thenReturn(Optional.empty());
        TokenQueryApplicationService service = new TokenQueryApplicationService(authSessionRepository, tokenCodec);

        assertFalse(service.verifyAccessToken(new TokenVerifyQuery("bad-token")).isValid());
    }

    @Test
    void shouldThrowNotFoundWhenSessionContextDoesNotExist() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.empty());
        TokenQueryApplicationService service = new TokenQueryApplicationService(authSessionRepository, tokenCodec);

        assertThrows(NotFoundException.class, () -> service.getSessionContext(new SessionContextQuery("session-1")));
    }
}
