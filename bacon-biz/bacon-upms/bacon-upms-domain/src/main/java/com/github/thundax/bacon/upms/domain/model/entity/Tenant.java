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
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Tenant create(
            TenantId id,
            String name,
            TenantCode tenantCode,
            TenantStatus status,
            Instant expiredAt,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(tenantCode, "tenantCode must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Tenant(id, name, tenantCode, status, expiredAt, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Tenant create(TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        return create(id, name, tenantCode, status, expiredAt, null, null, null, null);
    }

    public static Tenant reconstruct(
            TenantId id,
            String name,
            TenantCode tenantCode,
            TenantStatus status,
            Instant expiredAt,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        return new Tenant(id, name, tenantCode, status, expiredAt, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Tenant reconstruct(
            TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        return reconstruct(id, name, tenantCode, status, expiredAt, null, null, null, null);
    }
}
