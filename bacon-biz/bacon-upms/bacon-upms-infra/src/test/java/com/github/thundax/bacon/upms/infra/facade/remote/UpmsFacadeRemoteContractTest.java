package com.github.thundax.bacon.upms.infra.facade.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class UpmsFacadeRemoteContractTest {

    private static final String BASE_URL = "http://upms.test/api";
    private static final String PROVIDER_TOKEN = "upms-token";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallCurrentUserProviderPaths() {
        server.expect(requestTo(BASE_URL + "/providers/upms/queries/current-user"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 2001,
                          "account": "alice",
                          "name": "Alice",
                          "avatarStoredObjectNo": "storage-20260327100000-000501",
                          "phone": "13800000001",
                          "departmentCode": "RD",
                          "avatarUrl": "https://cdn.example.com/avatar.png",
                          "status": "ACTIVE"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/upms/queries/current-tenant"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "name": "Demo Tenant",
                          "code": "TENANT_DEMO",
                          "status": "ACTIVE",
                          "expiredAt": "2099-01-01T00:00:00Z"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/providers/upms/queries/current-data-scope"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "allAccess": false,
                          "scopeTypes": ["DEPARTMENT"],
                          "departmentIds": [11]
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        CurrentUserReadFacadeRemoteImpl facade = newCurrentUserFacade();
        UserFacadeResponse user = facade.getCurrentUser();
        TenantFacadeResponse tenant = facade.getCurrentTenant();
        UserDataScopeFacadeResponse dataScope = facade.getCurrentDataScope();

        assertThat(user.account()).isEqualTo("alice");
        assertThat(tenant.code()).isEqualTo("TENANT_DEMO");
        assertThat(dataScope.departmentIds()).containsExactly(11L);
        server.verify();
    }

    @Test
    void shouldCallUserCredentialProviderPaths() {
        server.expect(requestTo(BASE_URL
                        + "/providers/upms/queries/user-identity?identityType=ACCOUNT&identityValue=alice"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 301,
                          "userId": 2001,
                          "identityType": "ACCOUNT",
                          "identityValue": "alice",
                          "status": "ACTIVE"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL
                        + "/providers/upms/queries/user-credential?identityType=ACCOUNT&identityValue=alice"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "userId": 2001,
                          "identityId": 301,
                          "account": "alice",
                          "phone": "13800000001",
                          "identityType": "ACCOUNT",
                          "identityValue": "alice",
                          "identityStatus": "ACTIVE",
                          "credentialId": 401,
                          "credentialType": "PASSWORD",
                          "credentialStatus": "ACTIVE",
                          "needChangePassword": true,
                          "credentialExpiresAt": "2099-01-01T00:00:00Z",
                          "lockedUntil": null,
                          "mfaRequired": true,
                          "secondFactorTypes": ["TOTP"],
                          "status": "ACTIVE",
                          "passwordHash": "{noop}password"
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        UserCredentialReadFacadeRemoteImpl facade = newCredentialFacade();

        UserIdentityFacadeResponse identity =
                facade.getUserIdentity(new UserIdentityGetFacadeRequest("ACCOUNT", "alice"));
        UserCredentialFacadeResponse credential =
                facade.getUserCredential(new UserCredentialGetFacadeRequest("ACCOUNT", "alice"));

        assertThat(identity.identityValue()).isEqualTo("alice");
        assertThat(credential.credentialId()).isEqualTo(401L);
        assertThat(credential.secondFactorTypes()).containsExactly("TOTP");
        server.verify();
    }

    @Test
    void shouldCallChangePasswordProviderPathWithBody() {
        server.expect(requestTo(BASE_URL + "/providers/upms/commands/change-current-user-password"))
                .andExpect(header("X-Bacon-Provider-Token", PROVIDER_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        newPasswordFacade().changePassword(new UserPasswordChangeFacadeRequest("old-password", "new-password"));

        server.verify();
    }

    private CurrentUserReadFacadeRemoteImpl newCurrentUserFacade() {
        return new CurrentUserReadFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private UserCredentialReadFacadeRemoteImpl newCredentialFacade() {
        return new UserCredentialReadFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private UserPasswordFacadeRemoteImpl newPasswordFacade() {
        return new UserPasswordFacadeRemoteImpl(restClientFactory(), BASE_URL, PROVIDER_TOKEN);
    }

    private RestClientFactory restClientFactory() {
        @SuppressWarnings("unchecked")
        ObjectProvider<RestClient.Builder> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable(Mockito.any())).thenReturn(restClientBuilder);
        return new RestClientFactory(provider);
    }
}
