package com.github.thundax.bacon.auth.infra.facade.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class AuthFacadeRemoteContractTest {

    private static final String BASE_URL = "http://auth.test/api";
    private static final String PROVIDER_TOKEN = "auth-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallVerifyTokenProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/auth/queries/verify-token?accessToken=access-token"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "valid": true,
                          "tenantId": 1001,
                          "userId": 2001,
                          "sessionId": "session-1",
                          "identityId": 3001,
                          "identityType": "PHONE",
                          "expireAt": "2026-03-27T10:30:00Z"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        SessionValidationFacadeResponse response =
                newTokenFacade().verifyAccessToken(new TokenVerifyFacadeRequest("access-token"));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getUserId()).isEqualTo(2001L);
        assertThat(response.getSessionId()).isEqualTo("session-1");
        server.verify();
    }

    @Test
    void shouldCallSessionContextProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/auth/queries/session-context?sessionId=session-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "sessionId": "session-1",
                          "tenantId": 1001,
                          "userId": 2001,
                          "identityType": "PHONE",
                          "loginType": "SMS",
                          "sessionStatus": "ACTIVE",
                          "issuedAt": "2026-03-27T10:00:00Z",
                          "lastAccessTime": "2026-03-27T10:01:00Z",
                          "expireAt": "2026-03-27T10:30:00Z"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        CurrentSessionFacadeResponse response =
                newTokenFacade().getSessionContext(new SessionContextGetFacadeRequest("session-1"));

        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getSessionStatus()).isEqualTo("ACTIVE");
        server.verify();
    }

    @Test
    void shouldCallSessionCommandProviderPaths() {
        server.expect(requestTo(BASE_URL
                        + "/providers/auth/commands/invalidate-user-sessions?tenantId=1001&userId=2001&reason=USER_DISABLED"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL
                        + "/providers/auth/commands/invalidate-tenant-sessions?tenantId=1001&reason=TENANT_DISABLED"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());
        server.expect(requestTo(BASE_URL
                        + "/providers/auth/commands/invalidate-session?sessionId=session-1&reason=LOGOUT"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        SessionCommandFacadeRemoteImpl facade = newSessionCommandFacade();
        facade.invalidateUserSessions(new SessionInvalidateUserFacadeRequest(1001L, 2001L, "USER_DISABLED"));
        facade.invalidateTenantSessions(new SessionInvalidateTenantFacadeRequest(1001L, "TENANT_DISABLED"));
        facade.invalidateSession(new SessionInvalidateFacadeRequest("session-1", "LOGOUT"));

        server.verify();
    }

    @Test
    void shouldCallOAuthClientProviderPath() {
        server.expect(requestTo(BASE_URL + "/providers/auth/queries/oauth-client?clientId=client-1"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "clientId": "client-1",
                          "clientName": "Demo Client",
                          "grantTypes": ["authorization_code"],
                          "scopes": ["openid"],
                          "redirectUris": ["https://example.com/callback"],
                          "enabled": true
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        OAuthClientFacadeResponse response =
                newOAuthClientFacade().getClientByClientId(new OAuthClientGetFacadeRequest("client-1"));

        assertThat(response.getClientId()).isEqualTo("client-1");
        assertThat(response.getClientName()).isEqualTo("Demo Client");
        assertThat(response.getGrantTypes()).containsExactly("authorization_code");
        server.verify();
    }

    private TokenVerifyFacadeRemoteImpl newTokenFacade() {
        return new TokenVerifyFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private SessionCommandFacadeRemoteImpl newSessionCommandFacade() {
        return new SessionCommandFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private OAuthClientReadFacadeRemoteImpl newOAuthClientFacade() {
        return new OAuthClientReadFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private RestClientFactory restClientFactory() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RestClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable(Mockito.any())).thenReturn(restClientBuilder);
        return new RestClientFactory(provider);
    }
}
