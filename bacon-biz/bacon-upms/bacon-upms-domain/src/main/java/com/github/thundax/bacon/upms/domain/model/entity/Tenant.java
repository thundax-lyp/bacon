package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import lombok.Getter;

import java.time.Instant;

/**
 * 租户领域实体。
 */
@Getter
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

    public Tenant(String tenantId, String name, String tenantCode, TenantStatus status, Instant expiredAt) {
        this(TenantId.of(tenantId), name, TenantCode.of(tenantCode), status, expiredAt, null, null, null, null);
    }

    public Tenant(String tenantId, String name, String tenantCode, TenantStatus status, Instant expiredAt,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this(TenantId.of(tenantId), name, TenantCode.of(tenantCode), status, expiredAt,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public Tenant(TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt) {
        this(id, name, tenantCode, status, expiredAt, null, null, null, null);
    }

    public Tenant(TenantId id, String name, TenantCode tenantCode, TenantStatus status, Instant expiredAt,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.tenantCode = tenantCode;
        this.status = status;
        this.expiredAt = expiredAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
