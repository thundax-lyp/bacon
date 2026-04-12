package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 岗位领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post {

    /** 岗位主键。 */
    private PostId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 岗位编码。 */
    private String code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 岗位状态。 */
    private PostStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Post create(
            PostId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId departmentId,
            PostStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Post(id, tenantId, code, name, departmentId, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Post create(
            PostId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId departmentId,
            PostStatus status) {
        return create(id, tenantId, code, name, departmentId, status, null, null, null, null);
    }

    public static Post reconstruct(
            PostId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId departmentId,
            PostStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        return new Post(id, tenantId, code, name, departmentId, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Post reconstruct(
            PostId id,
            TenantId tenantId,
            String code,
            String name,
            DepartmentId departmentId,
            PostStatus status) {
        return reconstruct(id, tenantId, code, name, departmentId, status, null, null, null, null);
    }
}
