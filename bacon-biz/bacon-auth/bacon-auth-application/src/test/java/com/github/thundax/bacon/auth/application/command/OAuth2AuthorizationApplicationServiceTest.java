package com.github.thundax.bacon.auth.application.command;

import com.github.thundax.bacon.auth.domain.model.enums.ClientStatus;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2AuthorizationApplicationServiceTest {

    @Test
    void shouldAcceptHashedClientSecretInStrictRepositoryMode() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuthAuthorizationRepository authorizationRepository = mock(OAuthAuthorizationRepository.class);
        when(authorizationRepository.findOAuthRefreshTokenByHash("hash-refresh")).thenReturn(Optional.empty());
        OAuth2AuthorizationApplicationService service = new OAuth2AuthorizationApplicationService(
                oauthClientRepository(new OAuthClient(1L, "demo-client", passwordEncoder.encode("demo-secret"),
                        "Demo OAuth Client", "CONFIDENTIAL", List.of("refresh_token"), List.of("openid"), List.of(),
                        1800L, 2592000L, ClientStatus.ENABLED, null, null, Instant.now(), Instant.now())),
                authorizationRepository,
                mock(SessionApplicationService.class),
                tokenCodec("hash-refresh"),
                passwordEncoder);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.token("refresh_token", null, null, "demo-client", "demo-secret", null, "refresh"));
        assertEquals("OAuth refresh token invalid", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidClientSecret() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuth2AuthorizationApplicationService service = new OAuth2AuthorizationApplicationService(
                oauthClientRepository(new OAuthClient(1L, "demo-client", passwordEncoder.encode("demo-secret"),
                        "Demo OAuth Client", "CONFIDENTIAL", List.of("refresh_token"), List.of("openid"), List.of(),
                        1800L, 2592000L, ClientStatus.ENABLED, null, null, Instant.now(), Instant.now())),
                mock(OAuthAuthorizationRepository.class),
                mock(SessionApplicationService.class),
                mock(com.github.thundax.bacon.auth.application.support.TokenCodec.class),
                passwordEncoder);

        assertThrows(IllegalArgumentException.class,
                () -> service.token("refresh_token", null, null, "demo-client", "wrong-secret", null, "refresh"));
    }

    private OAuthClientRepository oauthClientRepository(OAuthClient client) {
        OAuthClientRepository repository = mock(OAuthClientRepository.class);
        when(repository.findByClientCode(client.getClientCodeValue())).thenReturn(Optional.of(client));
        return repository;
    }

    private com.github.thundax.bacon.auth.application.support.TokenCodec tokenCodec(String hashValue) {
        com.github.thundax.bacon.auth.application.support.TokenCodec tokenCodec =
                mock(com.github.thundax.bacon.auth.application.support.TokenCodec.class);
        when(tokenCodec.sha256("refresh")).thenReturn(hashValue);
        return tokenCodec;
    }
}
