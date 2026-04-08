package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 部门领域实体。
 */
@Getter
@AllArgsConstructor
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

    public Department(Long id, Long tenantId, String code, String name, Long parentId, Long leaderUserId,
                      Integer sort, DepartmentStatus status, String createdBy, Instant createdAt, String updatedBy,
                      Instant updatedAt) {
        this(id == null ? null : DepartmentId.of(id),
                tenantId == null ? null : TenantId.of(tenantId),
                code, name,
                parentId == null ? null : DepartmentId.of(parentId),
                leaderUserId == null ? null : UserId.of(leaderUserId),
                sort, status, createdBy, createdAt, updatedBy, updatedAt);
    }
}
