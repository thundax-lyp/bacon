package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
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
    private TenantCode tenantCode;
    /** 租户状态。 */
    private TenantStatus status;
    /** 过期时间。 */
    private Instant expiredAt;

    public static Tenant create(
            TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(tenantCode, "tenantCode must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Tenant(id, name, tenantCode, status, expiredAt);
    }

    public static Tenant reconstruct(
            TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        return new Tenant(id, name, tenantCode, status, expiredAt);
    }

    public Tenant update(String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(tenantCode, "tenantCode must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Tenant(id, name, tenantCode, status, expiredAt);
    }
}
