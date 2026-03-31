package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
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
    /** 租户状态。 */
    private TenantStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public Tenant(String tenantId, String name, TenantStatus status) {
        this(TenantId.of(tenantId), name, status, null, null, null, null);
    }

    public Tenant(String tenantId, String name, TenantStatus status,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this(TenantId.of(tenantId), name, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public Tenant(TenantId id, String name, TenantStatus status) {
        this(id, name, status, null, null, null, null);
    }

    public Tenant(TenantId id, String name, TenantStatus status,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
