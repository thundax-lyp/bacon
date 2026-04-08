package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租户领域实体。
 */
@Getter
@AllArgsConstructor
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

    public Tenant(Long tenantId, String name, String tenantCode, TenantStatus status, Instant expiredAt,
                  String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this(tenantId == null ? null : TenantId.of(tenantId), name, tenantCode == null ? null : TenantCode.of(tenantCode), status, expiredAt,
                createdBy, createdAt, updatedBy, updatedAt);
    }
}
