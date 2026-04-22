package com.github.thundax.bacon.auth.interfaces.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.OAuthClientQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.TokenQueryApplicationService;
import com.github.thundax.bacon.auth.interfaces.response.CurrentSessionResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuthClientResponse;
import com.github.thundax.bacon.auth.interfaces.response.SessionValidationResponse;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthProviderControllerTest {

    @Test
    void shouldReturnInterfaceResponseForVerify() {
        TokenQueryApplicationService tokenQueryApplicationService = mock(TokenQueryApplicationService.class);
        when(tokenQueryApplicationService.verifyAccessToken(any()))
                .thenReturn(new SessionValidationDTO(true, 1001L, 2001L, "session-1", 3001L, "PHONE", Instant.now()));

        AuthProviderController controller = new AuthProviderController(
                tokenQueryApplicationService,
                mock(SessionCommandApplicationService.class),
                mock(OAuthClientQueryApplicationService.class));

        SessionValidationResponse response = controller.verify("access-token");

        assertTrue(response.valid());
        assertEquals(1001L, response.tenantId());
        assertEquals(2001L, response.userId());
        assertEquals("session-1", response.sessionId());
    }

    @Test
    void shouldReturnInterfaceResponseForCurrentSession() {
        TokenQueryApplicationService tokenQueryApplicationService = mock(TokenQueryApplicationService.class);
        when(tokenQueryApplicationService.getSessionContext(any()))
                .thenReturn(new CurrentSessionDTO(
                        "session-1",
                        1001L,
                        2001L,
                        "PHONE",
                        "SMS",
                        "ACTIVE",
                        Instant.now(),
                        Instant.now(),
                        Instant.now().plusSeconds(1800)));

        AuthProviderController controller = new AuthProviderController(
                tokenQueryApplicationService,
                mock(SessionCommandApplicationService.class),
                mock(OAuthClientQueryApplicationService.class));

        CurrentSessionResponse response = controller.currentSession("session-1");

        assertEquals("session-1", response.sessionId());
        assertEquals(1001L, response.tenantId());
        assertEquals(2001L, response.userId());
        assertEquals("PHONE", response.identityType());
    }

    @Test
    void shouldReturnInterfaceResponseForGetClient() {
        OAuthClientQueryApplicationService clientQueryApplicationService = mock(OAuthClientQueryApplicationService.class);
        when(clientQueryApplicationService.getClientByClientId(any()))
                .thenReturn(new OAuthClientDTO(
                        "client-1",
                        "Demo Client",
                        Set.of("authorization_code"),
                        Set.of("openid"),
                        Set.of("https://example.com/callback"),
                        true));

        AuthProviderController controller = new AuthProviderController(
                mock(TokenQueryApplicationService.class),
                mock(SessionCommandApplicationService.class),
                clientQueryApplicationService);

        OAuthClientResponse response = controller.getClient("client-1");

        assertEquals("client-1", response.clientId());
        assertEquals("Demo Client", response.clientName());
        assertTrue(response.enabled());
    }
}
