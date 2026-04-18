package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TenantTest {

    @Test
    void shouldToggleTenantStatus() {
        Tenant tenant = Tenant.create(TenantId.of(101L), "Demo Tenant", TenantCode.of("TENANT_DEMO"), TenantStatus.ACTIVE, null);

        tenant.disable();
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.DISABLED);

        tenant.activate();
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldAllowActiveTenantBeforeExpiry() {
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2099-01-01T00:00:00Z"));

        assertThatCode(() -> tenant.assertActive(Instant.parse("2026-01-01T00:00:00Z"))).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInactiveTenant() {
        Tenant tenant = Tenant.create(TenantId.of(101L), "Demo Tenant", TenantCode.of("TENANT_DEMO"), TenantStatus.DISABLED, null);

        assertThatThrownBy(() -> tenant.assertActive(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Tenant is not active");
    }

    @Test
    void shouldRejectExpiredTenant() {
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2025-12-31T23:59:59Z"));

        assertThatThrownBy(() -> tenant.assertActive(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Tenant is expired");
    }

    @Test
    void shouldReportWhetherTenantIsActive() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Tenant activeTenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2099-01-01T00:00:00Z"));
        Tenant expiredTenant = Tenant.create(
                TenantId.of(102L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2025-12-31T23:59:59Z"));
        Tenant disabledTenant = Tenant.create(
                TenantId.of(103L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.DISABLED,
                Instant.parse("2099-01-01T00:00:00Z"));

        assertThat(activeTenant.isActive(now)).isTrue();
        assertThat(expiredTenant.isActive(now)).isFalse();
        assertThat(disabledTenant.isActive(now)).isFalse();
    }

    @Test
    void shouldReportWhetherTenantIsExpired() {
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(tenant.isExpired(Instant.parse("2025-12-31T23:59:59Z"))).isFalse();
        assertThat(tenant.isExpired(Instant.parse("2026-01-01T00:00:00Z"))).isTrue();
        assertThat(tenant.isExpired(Instant.parse("2026-01-01T00:00:01Z"))).isTrue();
    }

    @Test
    void shouldRenewTenantToNewExpiry() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Instant newExpiredAt = Instant.parse("2027-01-01T00:00:00Z");
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"));

        tenant.renewTo(newExpiredAt, now);

        assertThat(tenant.getExpiredAt()).isEqualTo(newExpiredAt);
    }

    @Test
    void shouldRenameChangeCodeAndClearExpiry() {
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"));

        tenant.rename("Demo Tenant New");
        tenant.recodeAs(TenantCode.of("TENANT_NEW"));
        tenant.clearExpiry();

        assertThat(tenant.getName()).isEqualTo("Demo Tenant New");
        assertThat(tenant.getCode()).isEqualTo(TenantCode.of("TENANT_NEW"));
        assertThat(tenant.getExpiredAt()).isNull();
    }

    @Test
    void shouldRejectRenewToPastTime() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Tenant tenant = Tenant.create(
                TenantId.of(101L),
                "Demo Tenant",
                TenantCode.of("TENANT_DEMO"),
                TenantStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"));

        assertThatThrownBy(() -> tenant.renewTo(now.minusSeconds(1), now))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Tenant expiredAt must be in future");
    }
}
