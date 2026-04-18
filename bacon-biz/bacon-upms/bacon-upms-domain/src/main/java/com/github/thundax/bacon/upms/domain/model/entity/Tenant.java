package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.exception.TenantErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 租户领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tenant {

    /** 租户主键。 */
    private TenantId id;
    /** 租户名称。 */
    private String name;
    /** 稳定业务编码。 */
    private TenantCode code;
    /** 租户状态。 */
    private TenantStatus status;
    /** 过期时间。 */
    private Instant expiredAt;

    public static Tenant create(
            TenantId id, String name, TenantCode code, TenantStatus status, Instant expiredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Tenant(id, name, code, status, expiredAt);
    }

    public static Tenant reconstruct(
            TenantId id, String name, TenantCode code, TenantStatus status, Instant expiredAt) {
        return new Tenant(id, name, code, status, expiredAt);
    }

    public void assertActive(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (status != TenantStatus.ACTIVE) {
            throw new UpmsDomainException(TenantErrorCode.TENANT_NOT_ACTIVE);
        }
        if (isExpired(now)) {
            throw new UpmsDomainException(TenantErrorCode.TENANT_EXPIRED);
        }
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return status == TenantStatus.ACTIVE && !isExpired(now);
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return expiredAt != null && !expiredAt.isAfter(now);
    }

    public void rename(String name) {
        Objects.requireNonNull(name, "name must not be null");
        this.name = name;
    }

    public void recodeAs(TenantCode code) {
        Objects.requireNonNull(code, "code must not be null");
        this.code = code;
    }

    public void renewTo(Instant newExpiredAt) {
        renewTo(newExpiredAt, Instant.now());
    }

    public void renewTo(Instant newExpiredAt, Instant now) {
        Objects.requireNonNull(newExpiredAt, "newExpiredAt must not be null");
        Objects.requireNonNull(now, "now must not be null");
        if (newExpiredAt.isBefore(now)) {
            throw new UpmsDomainException(TenantErrorCode.TENANT_INVALID_EXPIRED_AT);
        }
        this.expiredAt = newExpiredAt;
    }

    public void clearExpiry() {
        this.expiredAt = null;
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    public void disable() {
        this.status = TenantStatus.DISABLED;
    }
}
