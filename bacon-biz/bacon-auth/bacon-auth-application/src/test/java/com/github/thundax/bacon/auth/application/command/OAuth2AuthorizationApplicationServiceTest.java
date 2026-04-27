package com.github.thundax.bacon.auth.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.dto.OAuth2TokenDTO;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.domain.exception.AuthDomainException;
import com.github.thundax.bacon.auth.domain.exception.AuthErrorCode;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.model.enums.ClientStatus;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
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

        AuthDomainException exception = assertThrows(
                AuthDomainException.class,
                () -> service.token(new OAuth2TokenCommand(
                        "refresh_token", null, null, "demo-client", "demo-secret", null, "refresh")));
        assertEquals(AuthErrorCode.OAUTH_REFRESH_TOKEN_INVALID.code(), exception.getCode());
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

        AuthDomainException exception = assertThrows(
                AuthDomainException.class,
                () -> service.token(new OAuth2TokenCommand(
                        "refresh_token", null, null, "demo-client", "wrong-secret", null, "refresh")));
        assertEquals(AuthErrorCode.OAUTH_CLIENT_SECRET_INVALID.code(), exception.getCode());
    }

    @Test
    void shouldIssueOAuthTokensWhenAuthorizationCodeIsValid() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuthAuthorizationRepository authorizationRepository = mock(OAuthAuthorizationRepository.class);
        SessionQueryApplicationService sessionQueryApplicationService = mock(SessionQueryApplicationService.class);

        OAuthClient client = new OAuthClient(
                1L,
                "demo-client",
                passwordEncoder.encode("demo-secret"),
                "Demo OAuth Client",
                "CONFIDENTIAL",
                List.of("authorization_code", "refresh_token"),
                List.of("openid", "profile"),
                List.of("https://example.com/callback"),
                1800L,
                2592000L,
                ClientStatus.ENABLED,
                null,
                null,
                Instant.now(),
                Instant.now());
        OAuthAuthorizationRequest authorizationRequest = new OAuthAuthorizationRequest(
                "request-1",
                "demo-client",
                "https://example.com/callback",
                List.of("openid", "profile"),
                "state-1",
                null,
                null,
                1001L,
                2001L,
                Instant.now().plusSeconds(300));

        when(authorizationRepository.findByCode("auth-code")).thenReturn(Optional.of(authorizationRequest));
        when(authorizationRepository.update(any(com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken.class));
        when(authorizationRepository.update(any(com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken.class));

        com.github.thundax.bacon.auth.application.codec.TokenCodec tokenCodec =
                mock(com.github.thundax.bacon.auth.application.codec.TokenCodec.class);
        when(tokenCodec.randomToken()).thenReturn("access-token", "refresh-token");
        when(tokenCodec.sha256("access-token")).thenReturn("access-token-hash");
        when(tokenCodec.sha256("refresh-token")).thenReturn("refresh-token-hash");

        OAuth2AuthorizationCommandApplicationService service = new OAuth2AuthorizationCommandApplicationService(
                oauthClientRepository(client),
                authorizationRepository,
                sessionQueryApplicationService,
                tokenCodec,
                passwordEncoder);

        OAuth2TokenDTO result = service.token(new OAuth2TokenCommand(
                "authorization_code",
                "auth-code",
                "https://example.com/callback",
                "demo-client",
                "demo-secret",
                null,
                null));

        assertEquals("access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(1800L, result.getExpiresIn());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("openid profile", result.getScope());
        verify(authorizationRepository).update(any(com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken.class));
        verify(authorizationRepository).update(any(com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken.class));
    }

    @Test
    void shouldRevokeBothAccessAndRefreshTokensWhenTokenIsValid() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        OAuthAuthorizationRepository authorizationRepository = mock(OAuthAuthorizationRepository.class);
        OAuthClientRepository clientRepository = mock(OAuthClientRepository.class);
        OAuthClient client = new OAuthClient(
                1L,
                "demo-client",
                "demo-secret",
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
                Instant.now());
        when(clientRepository.findByClientCode(anyString())).thenReturn(Optional.of(client));
        OAuthAccessToken accessToken = new OAuthAccessToken(
                "access-token-id",
                "token-hash",
                "demo-client",
                1001L,
                2001L,
                List.of("openid"),
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(600));
        OAuthRefreshToken refreshToken = new OAuthRefreshToken(
                "refresh-token-id",
                "token-hash",
                "access-token-id",
                "demo-client",
                1001L,
                2001L,
                Instant.now().minusSeconds(10),
                Instant.now().plusSeconds(600));

        when(authorizationRepository.findAccessByHash("token-hash")).thenReturn(Optional.of(accessToken));
        when(authorizationRepository.findByHash("token-hash")).thenReturn(Optional.of(refreshToken));
        when(authorizationRepository.update(any(OAuthAccessToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, OAuthAccessToken.class));
        when(authorizationRepository.update(any(OAuthRefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, OAuthRefreshToken.class));
        com.github.thundax.bacon.auth.application.codec.TokenCodec tokenCodec =
                mock(com.github.thundax.bacon.auth.application.codec.TokenCodec.class);
        when(tokenCodec.sha256("token")).thenReturn("token-hash");

        OAuth2AuthorizationCommandApplicationService service = new OAuth2AuthorizationCommandApplicationService(
                clientRepository,
                authorizationRepository,
                mock(SessionQueryApplicationService.class),
                tokenCodec,
                passwordEncoder);

        service.revoke(new OAuth2RevokeCommand("token", "demo-client", "demo-secret"));

        assertEquals("REVOKED", accessToken.getStatusValue());
        assertEquals("REVOKED", refreshToken.getTokenStatus());
        verify(authorizationRepository).update(accessToken);
        verify(authorizationRepository).update(refreshToken);
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
