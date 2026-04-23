package com.github.thundax.bacon.auth.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.auth.api.request.OAuthClientGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.OAuthClientFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthFacadeContractTest {

    @Test
    void shouldKeepTokenVerifyRequestContract() throws Exception {
        assertField(TokenVerifyFacadeRequest.class, "accessToken", String.class);

        assertThat(new TokenVerifyFacadeRequest("access-token").getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void shouldKeepSessionCommandRequestContracts() throws Exception {
        assertField(SessionContextGetFacadeRequest.class, "sessionId", String.class);
        assertField(SessionInvalidateUserFacadeRequest.class, "tenantId", Long.class);
        assertField(SessionInvalidateUserFacadeRequest.class, "userId", Long.class);
        assertField(SessionInvalidateUserFacadeRequest.class, "reason", String.class);
        assertField(SessionInvalidateTenantFacadeRequest.class, "tenantId", Long.class);
        assertField(SessionInvalidateTenantFacadeRequest.class, "reason", String.class);
        assertField(SessionInvalidateFacadeRequest.class, "sessionId", String.class);
        assertField(SessionInvalidateFacadeRequest.class, "reason", String.class);

        assertThat(new SessionContextGetFacadeRequest("session-1").getSessionId()).isEqualTo("session-1");
        assertThat(new SessionInvalidateUserFacadeRequest(1001L, 2001L, "USER_DISABLED").getUserId())
                .isEqualTo(2001L);
        assertThat(new SessionInvalidateTenantFacadeRequest(1001L, "TENANT_DISABLED").getTenantId())
                .isEqualTo(1001L);
        assertThat(new SessionInvalidateFacadeRequest("session-1", "LOGOUT").getReason())
                .isEqualTo("LOGOUT");
    }

    @Test
    void shouldKeepSessionValidationResponseContract() throws Exception {
        assertField(SessionValidationFacadeResponse.class, "valid", boolean.class);
        assertField(SessionValidationFacadeResponse.class, "tenantId", Long.class);
        assertField(SessionValidationFacadeResponse.class, "userId", Long.class);
        assertField(SessionValidationFacadeResponse.class, "sessionId", String.class);
        assertField(SessionValidationFacadeResponse.class, "identityId", Long.class);
        assertField(SessionValidationFacadeResponse.class, "identityType", String.class);
        assertField(SessionValidationFacadeResponse.class, "expireAt", Instant.class);

        SessionValidationFacadeResponse response = new SessionValidationFacadeResponse(
                true, 1001L, 2001L, "session-1", 3001L, "PHONE", Instant.parse("2026-03-27T10:30:00Z"));

        assertThat(response.isValid()).isTrue();
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getUserId()).isEqualTo(2001L);
        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getIdentityId()).isEqualTo(3001L);
        assertThat(response.getIdentityType()).isEqualTo("PHONE");
        assertThat(response.getExpireAt()).isEqualTo(Instant.parse("2026-03-27T10:30:00Z"));
    }

    @Test
    void shouldKeepCurrentSessionResponseContract() throws Exception {
        assertField(CurrentSessionFacadeResponse.class, "sessionId", String.class);
        assertField(CurrentSessionFacadeResponse.class, "tenantId", Long.class);
        assertField(CurrentSessionFacadeResponse.class, "userId", Long.class);
        assertField(CurrentSessionFacadeResponse.class, "identityType", String.class);
        assertField(CurrentSessionFacadeResponse.class, "loginType", String.class);
        assertField(CurrentSessionFacadeResponse.class, "sessionStatus", String.class);
        assertField(CurrentSessionFacadeResponse.class, "issuedAt", Instant.class);
        assertField(CurrentSessionFacadeResponse.class, "lastAccessTime", Instant.class);
        assertField(CurrentSessionFacadeResponse.class, "expireAt", Instant.class);

        CurrentSessionFacadeResponse response = new CurrentSessionFacadeResponse(
                "session-1",
                1001L,
                2001L,
                "PHONE",
                "SMS",
                "ACTIVE",
                Instant.parse("2026-03-27T10:00:00Z"),
                Instant.parse("2026-03-27T10:01:00Z"),
                Instant.parse("2026-03-27T10:30:00Z"));

        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getTenantId()).isEqualTo(1001L);
        assertThat(response.getUserId()).isEqualTo(2001L);
        assertThat(response.getIdentityType()).isEqualTo("PHONE");
        assertThat(response.getLoginType()).isEqualTo("SMS");
        assertThat(response.getSessionStatus()).isEqualTo("ACTIVE");
        assertThat(response.getLastAccessTime()).isEqualTo(Instant.parse("2026-03-27T10:01:00Z"));
    }

    @Test
    void shouldKeepOAuthClientRequestAndResponseContracts() throws Exception {
        assertField(OAuthClientGetFacadeRequest.class, "clientId", String.class);
        assertField(OAuthClientFacadeResponse.class, "clientId", String.class);
        assertField(OAuthClientFacadeResponse.class, "clientName", String.class);
        assertField(OAuthClientFacadeResponse.class, "grantTypes", Set.class);
        assertField(OAuthClientFacadeResponse.class, "scopes", Set.class);
        assertField(OAuthClientFacadeResponse.class, "redirectUris", Set.class);
        assertField(OAuthClientFacadeResponse.class, "enabled", boolean.class);

        OAuthClientFacadeResponse response = new OAuthClientFacadeResponse(
                "client-1",
                "Demo Client",
                Set.of("authorization_code"),
                Set.of("openid"),
                Set.of("https://example.com/callback"),
                true);

        assertThat(new OAuthClientGetFacadeRequest("client-1").getClientId()).isEqualTo("client-1");
        assertThat(response.getClientId()).isEqualTo("client-1");
        assertThat(response.getClientName()).isEqualTo("Demo Client");
        assertThat(response.getGrantTypes()).containsExactly("authorization_code");
        assertThat(response.getScopes()).containsExactly("openid");
        assertThat(response.getRedirectUris()).containsExactly("https://example.com/callback");
        assertThat(response.isEnabled()).isTrue();
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
