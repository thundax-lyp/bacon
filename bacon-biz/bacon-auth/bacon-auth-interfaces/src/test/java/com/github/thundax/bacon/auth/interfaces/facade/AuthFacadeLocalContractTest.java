package com.github.thundax.bacon.auth.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.query.OAuthClientQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.TokenQueryApplicationService;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthFacadeLocalContractTest {

    @Mock
    private TokenQueryApplicationService tokenQueryApplicationService;

    @Mock
    private SessionCommandApplicationService sessionCommandApplicationService;

    @Mock
    private OAuthClientQueryApplicationService oAuthClientQueryApplicationService;

    @Test
    void shouldMapTokenVerifyFacadeToApplicationQuery() {
        when(tokenQueryApplicationService.verifyAccessToken(argThat(query ->
                        query != null && "access-token".equals(query.accessToken()))))
                .thenReturn(new SessionValidationDTO(
                        true, 1001L, 2001L, "session-1", 3001L, "PHONE", Instant.parse("2026-03-27T10:30:00Z")));
        TokenVerifyFacadeLocalImpl facade = new TokenVerifyFacadeLocalImpl(tokenQueryApplicationService);

        SessionValidationFacadeResponse response =
                facade.verifyAccessToken(new TokenVerifyFacadeRequest("access-token"));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getUserId()).isEqualTo(2001L);
        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getIdentityId()).isEqualTo(3001L);
        assertThat(response.getIdentityType()).isEqualTo("PHONE");
    }

    @Test
    void shouldMapSessionContextFacadeToApplicationQuery() {
        when(tokenQueryApplicationService.getSessionContext(argThat(query ->
                        query != null && "session-1".equals(query.sessionId()))))
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
        TokenVerifyFacadeLocalImpl facade = new TokenVerifyFacadeLocalImpl(tokenQueryApplicationService);

        CurrentSessionFacadeResponse response = facade.getSessionContext(new SessionContextGetFacadeRequest("session-1"));

        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getUserId()).isEqualTo(2001L);
        assertThat(response.getSessionStatus()).isEqualTo("ACTIVE");
        assertThat(response.getLastAccessTime()).isEqualTo(Instant.parse("2026-03-27T10:01:00Z"));
    }

    @Test
    void shouldMapSessionCommandFacadeToApplicationCommands() {
        SessionCommandFacadeLocalImpl facade = new SessionCommandFacadeLocalImpl(sessionCommandApplicationService);

        facade.invalidateUserSessions(new SessionInvalidateUserFacadeRequest(1001L, 2001L, "USER_DISABLED"));
        facade.invalidateTenantSessions(new SessionInvalidateTenantFacadeRequest(1001L, "TENANT_DISABLED"));
        facade.invalidateSession(new SessionInvalidateFacadeRequest("session-1", "LOGOUT"));

        verify(sessionCommandApplicationService)
                .invalidateUserSessions(argThat(command -> command != null
                        && command.tenantId().equals(1001L)
                        && command.userId().equals(2001L)
                        && command.reason().equals("USER_DISABLED")));
        verify(sessionCommandApplicationService)
                .invalidateTenantSessions(argThat(command -> command != null
                        && command.tenantId().equals(1001L)
                        && command.reason().equals("TENANT_DISABLED")));
        verify(sessionCommandApplicationService)
                .invalidateSession(argThat(command -> command != null
                        && command.sessionId().equals("session-1")
                        && command.reason().equals("LOGOUT")));
    }

    @Test
    void shouldMapOAuthClientFacadeToApplicationQuery() {
        when(oAuthClientQueryApplicationService.getClientByClientId(argThat(query ->
                        query != null && "client-1".equals(query.clientId()))))
                .thenReturn(new OAuthClientDTO(
                        "client-1",
                        "Demo Client",
                        Set.of("authorization_code"),
                        Set.of("openid"),
                        Set.of("https://example.com/callback"),
                        true));
        OAuthClientReadFacadeLocalImpl facade = new OAuthClientReadFacadeLocalImpl(oAuthClientQueryApplicationService);

        OAuthClientFacadeResponse response = facade.getClientByClientId(new OAuthClientGetFacadeRequest("client-1"));

        assertThat(response.getClientId()).isEqualTo("client-1");
        assertThat(response.getClientName()).isEqualTo("Demo Client");
        assertThat(response.getGrantTypes()).containsExactly("authorization_code");
        assertThat(response.getScopes()).containsExactly("openid");
        assertThat(response.getRedirectUris()).containsExactly("https://example.com/callback");
        assertThat(response.isEnabled()).isTrue();
    }
}
