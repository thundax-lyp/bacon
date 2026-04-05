package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import lombok.Getter;

import java.time.Instant;

/**
 * 角色领域实体。
 */
@Getter
public class Role {

    /** 角色主键。 */
    private RoleId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 角色编码。 */
    private String code;
    /** 角色名称。 */
    private String name;
    /** 角色类型。 */
    private RoleType roleType;
    /** 数据范围类型。 */
    private RoleDataScopeType dataScopeType;
    /** 角色状态。 */
    private RoleStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public Role(RoleId id, TenantId tenantId, String code, String name, RoleType roleType,
                RoleDataScopeType dataScopeType, RoleStatus status) {
        this(id, tenantId, code, name, roleType, dataScopeType, status, null, null, null, null);
    }

    public Role(RoleId id, TenantId tenantId, String code, String name, RoleType roleType, RoleDataScopeType dataScopeType,
                RoleStatus status, String createdBy, Instant createdAt, String updatedBy,
                Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.roleType = roleType;
        this.dataScopeType = dataScopeType;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
