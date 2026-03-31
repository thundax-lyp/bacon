package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 租户领域实体。
 */
@Getter
public class Tenant {

    /** 租户主键。 */
    private Long id;
    /** 业务租户标识。 */
    private Long tenantId;
    /** 租户编码。 */
    private String code;
    /** 租户名称。 */
    private String name;
    /** 租户状态。 */
    private String status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public Tenant(Long id, Long tenantId, String code, String name, String status) {
        this(id, tenantId, code, name, status, null, null, null, null);
    }

    public Tenant(Long id, Long tenantId, String code, String name, String status,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
