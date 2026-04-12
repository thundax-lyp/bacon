package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 角色领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static Role create(
            RoleId id,
            TenantId tenantId,
            String code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(roleType, "roleType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Role(id, tenantId, code, name, roleType, dataScopeType, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Role create(
            RoleId id,
            TenantId tenantId,
            String code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status) {
        return create(id, tenantId, code, name, roleType, dataScopeType, status, null, null, null, null);
    }

    public static Role reconstruct(
            RoleId id,
            TenantId tenantId,
            String code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        return new Role(id, tenantId, code, name, roleType, dataScopeType, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Role reconstruct(
            RoleId id,
            TenantId tenantId,
            String code,
            String name,
            RoleType roleType,
            RoleDataScopeType dataScopeType,
            RoleStatus status) {
        return reconstruct(id, tenantId, code, name, roleType, dataScopeType, status, null, null, null, null);
    }
}
