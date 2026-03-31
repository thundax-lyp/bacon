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
    private Long id;
    /** 租户编号。 */
    private TenantId tenantId;
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

    public Tenant(Long id, String tenantNo, String name, TenantStatus status) {
        this(id, TenantId.of(tenantNo), name, status, null, null, null, null);
    }

    public Tenant(Long id, String tenantNo, String name, TenantStatus status,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this(id, TenantId.of(tenantNo), name, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public Tenant(Long id, TenantId tenantId, String name, TenantStatus status) {
        this(id, tenantId, name, status, null, null, null, null);
    }

    public Tenant(Long id, TenantId tenantId, String name, TenantStatus status,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getTenantNo() {
        return tenantId.value();
    }
}
