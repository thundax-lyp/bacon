package com.github.thundax.bacon.auth.interfaces.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.OAuthClientQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.TokenQueryApplicationService;
import com.github.thundax.bacon.auth.interfaces.response.CurrentSessionResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuthClientResponse;
import com.github.thundax.bacon.auth.interfaces.response.SessionValidationResponse;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthProviderControllerTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "auth-token";

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

    @Test
    void shouldExposeRawVerifyTokenProviderPayload() throws Exception {
        TokenQueryApplicationService tokenQueryApplicationService = mock(TokenQueryApplicationService.class);
        when(tokenQueryApplicationService.verifyAccessToken(any()))
                .thenReturn(new SessionValidationDTO(
                        true,
                        1001L,
                        2001L,
                        "session-1",
                        3001L,
                        "PHONE",
                        Instant.parse("2026-03-27T10:30:00Z")));

        newMockMvc(tokenQueryApplicationService, mock(SessionCommandApplicationService.class), mock(OAuthClientQueryApplicationService.class))
                .perform(get("/providers/auth/queries/verify-token")
                        .param("accessToken", "access-token")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.tenantId").value(1001))
                .andExpect(jsonPath("$.userId").value(2001))
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeRawCurrentSessionProviderPayload() throws Exception {
        TokenQueryApplicationService tokenQueryApplicationService = mock(TokenQueryApplicationService.class);
        when(tokenQueryApplicationService.getSessionContext(any()))
                .thenReturn(new CurrentSessionDTO(
                        "session-1",
                        1001L,
                        2001L,
                        "PHONE",
                        "SMS",
                        "ACTIVE",
                        Instant.parse("2026-03-27T10:00:00Z"),
                        Instant.parse("2026-03-27T10:01:00Z"),
                        Instant.parse("2026-03-27T10:30:00Z")));

        newMockMvc(tokenQueryApplicationService, mock(SessionCommandApplicationService.class), mock(OAuthClientQueryApplicationService.class))
                .perform(get("/providers/auth/queries/session-context")
                        .param("sessionId", "session-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-1"))
                .andExpect(jsonPath("$.tenantId").value(1001))
                .andExpect(jsonPath("$.sessionStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeRawOAuthClientProviderPayload() throws Exception {
        OAuthClientQueryApplicationService clientQueryApplicationService = mock(OAuthClientQueryApplicationService.class);
        when(clientQueryApplicationService.getClientByClientId(any()))
                .thenReturn(new OAuthClientDTO(
                        "client-1",
                        "Demo Client",
                        Set.of("authorization_code"),
                        Set.of("openid"),
                        Set.of("https://example.com/callback"),
                        true));

        newMockMvc(mock(TokenQueryApplicationService.class), mock(SessionCommandApplicationService.class), clientQueryApplicationService)
                .perform(get("/providers/auth/queries/oauth-client")
                        .param("clientId", "client-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client-1"))
                .andExpect(jsonPath("$.clientName").value("Demo Client"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenProviderRequiredParamMissing() throws Exception {
        newMockMvc(
                        mock(TokenQueryApplicationService.class),
                        mock(SessionCommandApplicationService.class),
                        mock(OAuthClientQueryApplicationService.class))
                .perform(get("/providers/auth/queries/verify-token").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProviderCallWhenTokenMissing() throws Exception {
        newMockMvc(
                        mock(TokenQueryApplicationService.class),
                        mock(SessionCommandApplicationService.class),
                        mock(OAuthClientQueryApplicationService.class))
                .perform(get("/providers/auth/queries/verify-token").param("accessToken", "access-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProviderCallWhenTokenInvalid() throws Exception {
        newMockMvc(
                        mock(TokenQueryApplicationService.class),
                        mock(SessionCommandApplicationService.class),
                        mock(OAuthClientQueryApplicationService.class))
                .perform(get("/providers/auth/queries/verify-token")
                        .param("accessToken", "access-token")
                        .header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAcceptSessionInvalidateProviderCommand() throws Exception {
        newMockMvc(
                        mock(TokenQueryApplicationService.class),
                        mock(SessionCommandApplicationService.class),
                        mock(OAuthClientQueryApplicationService.class))
                .perform(post("/providers/auth/commands/invalidate-user-sessions")
                        .param("tenantId", "1001")
                        .param("userId", "2001")
                        .param("reason", "USER_DISABLED")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
    }

    private MockMvc newMockMvc(
            TokenQueryApplicationService tokenQueryApplicationService,
            SessionCommandApplicationService sessionCommandApplicationService,
            OAuthClientQueryApplicationService oAuthClientQueryApplicationService) {
        return MockMvcBuilders.standaloneSetup(new AuthProviderController(
                        tokenQueryApplicationService,
                        sessionCommandApplicationService,
                        oAuthClientQueryApplicationService))
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/auth/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }
}
