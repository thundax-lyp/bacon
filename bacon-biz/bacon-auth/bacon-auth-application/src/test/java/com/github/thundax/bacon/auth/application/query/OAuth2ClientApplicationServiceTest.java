package com.github.thundax.bacon.auth.application.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OAuthClientQueryApplicationServiceTest {

    @Test
    void shouldThrowNotFoundWhenClientDoesNotExist() {
        OAuthClientRepository repository = mock(OAuthClientRepository.class);
        when(repository.findByClientCode("missing-client")).thenReturn(Optional.empty());
        OAuthClientQueryApplicationService service = new OAuthClientQueryApplicationService(repository);

        AuthDomainException exception = assertThrows(
                AuthDomainException.class,
                () -> service.getClientByClientId(new OAuthClientQuery("missing-client")));
        assertEquals(AuthErrorCode.OAUTH_CLIENT_INVALID.code(), exception.getCode());
    }
}
