package com.github.thundax.bacon.auth.application.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.codec.TokenCodec;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Instant;
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

        AuthDomainException exception = assertThrows(
                AuthDomainException.class, () -> service.getSessionContext(new SessionContextQuery("session-1")));
        assertEquals(AuthErrorCode.SESSION_NOT_FOUND.code(), exception.getCode());
    }

    @Test
    void shouldReturnValidSessionAndTouchItWhenAccessTokenIsValid() {
        AuthSessionRepository authSessionRepository = mock(AuthSessionRepository.class);
        TokenCodec tokenCodec = mock(TokenCodec.class);
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
        when(tokenCodec.parseSessionId("access-token")).thenReturn(Optional.of("session-1"));
        when(authSessionRepository.findBySessionId("session-1")).thenReturn(Optional.of(authSession));
        when(authSessionRepository.update(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, AuthSession.class));
        TokenQueryApplicationService service = new TokenQueryApplicationService(authSessionRepository, tokenCodec);

        SessionValidationDTO result = service.verifyAccessToken(new TokenVerifyQuery("access-token"));

        assertTrue(result.isValid());
        assertEquals(1001L, result.getTenantId());
        assertEquals(2001L, result.getUserId());
        assertEquals("session-1", result.getSessionId());
        assertEquals(3001L, result.getIdentityId());
        assertEquals("ACCOUNT", result.getIdentityType());
        assertTrue(authSession.getLastAccessTime().isAfter(authSession.getIssuedAt()));
        verify(authSessionRepository).update(any(AuthSession.class));
    }
}
