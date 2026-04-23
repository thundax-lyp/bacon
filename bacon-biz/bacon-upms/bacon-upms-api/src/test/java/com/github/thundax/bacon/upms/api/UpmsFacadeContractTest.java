package com.github.thundax.bacon.upms.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.upms.api.request.UserCredentialGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserIdentityGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.UserPasswordChangeFacadeRequest;
import com.github.thundax.bacon.upms.api.response.TenantFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserCredentialFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserDataScopeFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserFacadeResponse;
import com.github.thundax.bacon.upms.api.response.UserIdentityFacadeResponse;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UpmsFacadeContractTest {

    @Test
    void shouldKeepUserCredentialRequestContracts() throws Exception {
        assertField(UserIdentityGetFacadeRequest.class, "identityType", String.class);
        assertField(UserIdentityGetFacadeRequest.class, "identityValue", String.class);
        assertField(UserCredentialGetFacadeRequest.class, "identityType", String.class);
        assertField(UserCredentialGetFacadeRequest.class, "identityValue", String.class);

        assertThat(new UserIdentityGetFacadeRequest("ACCOUNT", "alice").getIdentityType())
                .isEqualTo("ACCOUNT");
        assertThat(new UserCredentialGetFacadeRequest("ACCOUNT", "alice").getIdentityValue())
                .isEqualTo("alice");
    }

    @Test
    void shouldKeepUserPasswordRequestContract() throws Exception {
        assertField(UserPasswordChangeFacadeRequest.class, "oldPassword", String.class);
        assertField(UserPasswordChangeFacadeRequest.class, "newPassword", String.class);

        UserPasswordChangeFacadeRequest request =
                new UserPasswordChangeFacadeRequest("old-password", "new-password");

        assertThat(request.getOldPassword()).isEqualTo("old-password");
        assertThat(request.getNewPassword()).isEqualTo("new-password");
    }

    @Test
    void shouldKeepCurrentUserResponseContract() throws Exception {
        assertField(UserFacadeResponse.class, "id", Long.class);
        assertField(UserFacadeResponse.class, "account", String.class);
        assertField(UserFacadeResponse.class, "name", String.class);
        assertField(UserFacadeResponse.class, "avatarStoredObjectNo", String.class);
        assertField(UserFacadeResponse.class, "phone", String.class);
        assertField(UserFacadeResponse.class, "departmentCode", String.class);
        assertField(UserFacadeResponse.class, "avatarUrl", String.class);
        assertField(UserFacadeResponse.class, "status", String.class);

        UserFacadeResponse response = new UserFacadeResponse(
                2001L,
                "alice",
                "Alice",
                "storage-20260327100000-000501",
                "13800000001",
                "RD",
                "https://cdn.example.com/avatar.png",
                "ACTIVE");

        assertThat(response.id()).isEqualTo(2001L);
        assertThat(response.departmentCode()).isEqualTo("RD");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example.com/avatar.png");
    }

    @Test
    void shouldKeepTenantAndDataScopeResponseContracts() throws Exception {
        assertField(TenantFacadeResponse.class, "name", String.class);
        assertField(TenantFacadeResponse.class, "code", String.class);
        assertField(TenantFacadeResponse.class, "status", String.class);
        assertField(TenantFacadeResponse.class, "expiredAt", Instant.class);
        assertField(UserDataScopeFacadeResponse.class, "allAccess", boolean.class);
        assertField(UserDataScopeFacadeResponse.class, "scopeTypes", Set.class);
        assertField(UserDataScopeFacadeResponse.class, "departmentIds", Set.class);

        TenantFacadeResponse tenant =
                new TenantFacadeResponse("Demo Tenant", "TENANT_DEMO", "ACTIVE", Instant.parse("2099-01-01T00:00:00Z"));
        UserDataScopeFacadeResponse dataScope =
                new UserDataScopeFacadeResponse(false, Set.of("DEPARTMENT"), Set.of(11L));

        assertThat(tenant.code()).isEqualTo("TENANT_DEMO");
        assertThat(dataScope.allAccess()).isFalse();
        assertThat(dataScope.departmentIds()).containsExactly(11L);
    }

    @Test
    void shouldKeepIdentityAndCredentialResponseContracts() throws Exception {
        assertField(UserIdentityFacadeResponse.class, "id", Long.class);
        assertField(UserIdentityFacadeResponse.class, "userId", Long.class);
        assertField(UserIdentityFacadeResponse.class, "identityType", String.class);
        assertField(UserIdentityFacadeResponse.class, "identityValue", String.class);
        assertField(UserIdentityFacadeResponse.class, "status", String.class);
        assertField(UserCredentialFacadeResponse.class, "userId", Long.class);
        assertField(UserCredentialFacadeResponse.class, "identityId", Long.class);
        assertField(UserCredentialFacadeResponse.class, "credentialId", Long.class);
        assertField(UserCredentialFacadeResponse.class, "needChangePassword", boolean.class);
        assertField(UserCredentialFacadeResponse.class, "secondFactorTypes", List.class);
        assertField(UserCredentialFacadeResponse.class, "passwordHash", String.class);

        UserIdentityFacadeResponse identity =
                new UserIdentityFacadeResponse(301L, 2001L, "ACCOUNT", "alice", "ACTIVE");
        UserCredentialFacadeResponse credential = new UserCredentialFacadeResponse(
                2001L,
                301L,
                "alice",
                "13800000001",
                "ACCOUNT",
                "alice",
                "ACTIVE",
                401L,
                "PASSWORD",
                "ACTIVE",
                true,
                Instant.parse("2099-01-01T00:00:00Z"),
                null,
                true,
                List.of("TOTP"),
                "ACTIVE",
                "{noop}password");

        assertThat(identity.identityValue()).isEqualTo("alice");
        assertThat(credential.credentialId()).isEqualTo(401L);
        assertThat(credential.needChangePassword()).isTrue();
        assertThat(credential.secondFactorTypes()).containsExactly("TOTP");
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
