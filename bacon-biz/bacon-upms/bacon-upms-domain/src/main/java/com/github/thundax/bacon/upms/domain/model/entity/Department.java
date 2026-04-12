package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 部门领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Department {

    /** 部门主键。 */
    private DepartmentId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 部门编码。 */
    private String code;
    /** 部门名称。 */
    private String name;
    /** 父部门主键，根部门固定为 0。 */
    private DepartmentId parentId;
    /** 部门负责人用户主键。 */
    private UserId leaderUserId;
    /** 排序值。 */
    private Integer sort;
    /** 部门状态。 */
    private DepartmentStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Department create(
            DepartmentId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Department(
                id, tenantId, code, name, parentId, leaderUserId, sort, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Department create(
            DepartmentId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        return create(id, tenantId, code, name, parentId, leaderUserId, sort, status, null, null, null, null);
    }

    public static Department reconstruct(
            DepartmentId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        return new Department(
                id, tenantId, code, name, parentId, leaderUserId, sort, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Department reconstruct(
            DepartmentId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort,
            DepartmentStatus status) {
        return reconstruct(id, tenantId, code, name, parentId, leaderUserId, sort, status, null, null, null, null);
    }
}
