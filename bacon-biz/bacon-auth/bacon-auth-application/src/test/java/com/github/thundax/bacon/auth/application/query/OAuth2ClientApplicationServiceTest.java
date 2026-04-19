package com.github.thundax.bacon.auth.application.query;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OAuth2ClientApplicationServiceTest {

    @Test
    void shouldThrowNotFoundWhenClientDoesNotExist() {
        OAuthClientRepository repository = mock(OAuthClientRepository.class);
        when(repository.findByClientCode("missing-client")).thenReturn(Optional.empty());
        OAuth2ClientApplicationService service = new OAuth2ClientApplicationService(repository);

        assertThrows(NotFoundException.class, () -> service.getClientByClientId("missing-client"));
    }
}
