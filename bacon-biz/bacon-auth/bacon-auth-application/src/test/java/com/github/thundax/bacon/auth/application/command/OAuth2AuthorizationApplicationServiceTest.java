package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.enums.ClientStatus;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class OAuth2AuthorizationCommandApplicationServiceTest {

    @Test
    void shouldAcceptHashedClientSecretInStrictRepositoryMode() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuthAuthorizationRepository authorizationRepository = mock(OAuthAuthorizationRepository.class);
        when(authorizationRepository.findByHash("hash-refresh"))
                .thenReturn(Optional.empty());
        OAuth2AuthorizationCommandApplicationService service = new OAuth2AuthorizationCommandApplicationService(
                oauthClientRepository(new OAuthClient(
                        1L,
                        "demo-client",
                        passwordEncoder.encode("demo-secret"),
                        "Demo OAuth Client",
                        "CONFIDENTIAL",
                        List.of("refresh_token"),
                        List.of("openid"),
                        List.of(),
                        1800L,
                        2592000L,
                        ClientStatus.ENABLED,
                        null,
                        null,
                        Instant.now(),
                        Instant.now())),
                authorizationRepository,
                mock(SessionQueryApplicationService.class),
                tokenCodec("hash-refresh"),
                passwordEncoder);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.token(new OAuth2TokenCommand(
                        "refresh_token", null, null, "demo-client", "demo-secret", null, "refresh")));
        assertEquals("OAuth refresh token invalid", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidClientSecret() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuth2AuthorizationCommandApplicationService service = new OAuth2AuthorizationCommandApplicationService(
                oauthClientRepository(new OAuthClient(
                        1L,
                        "demo-client",
                        passwordEncoder.encode("demo-secret"),
                        "Demo OAuth Client",
                        "CONFIDENTIAL",
                        List.of("refresh_token"),
                        List.of("openid"),
                        List.of(),
                        1800L,
                        2592000L,
                        ClientStatus.ENABLED,
                        null,
                        null,
                        Instant.now(),
                        Instant.now())),
                mock(OAuthAuthorizationRepository.class),
                mock(SessionQueryApplicationService.class),
                mock(com.github.thundax.bacon.auth.application.codec.TokenCodec.class),
                passwordEncoder);

        assertThrows(
                BadRequestException.class,
                () -> service.token(new OAuth2TokenCommand(
                        "refresh_token", null, null, "demo-client", "wrong-secret", null, "refresh")));
    }

    private OAuthClientRepository oauthClientRepository(OAuthClient client) {
        OAuthClientRepository repository = mock(OAuthClientRepository.class);
        when(repository.findByClientCode(client.getClientCodeValue())).thenReturn(Optional.of(client));
        return repository;
    }

    private com.github.thundax.bacon.auth.application.codec.TokenCodec tokenCodec(String hashValue) {
        com.github.thundax.bacon.auth.application.codec.TokenCodec tokenCodec =
                mock(com.github.thundax.bacon.auth.application.codec.TokenCodec.class);
        when(tokenCodec.sha256("refresh")).thenReturn(hashValue);
        return tokenCodec;
    }
}
