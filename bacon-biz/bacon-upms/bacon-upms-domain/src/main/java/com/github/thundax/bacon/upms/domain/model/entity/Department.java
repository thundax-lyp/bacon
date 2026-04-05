package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import lombok.Getter;

import java.time.Instant;

/**
 * 部门领域实体。
 */
@Getter
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
    private String status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public Department(DepartmentId id, TenantId tenantId, String code, String name, DepartmentId parentId,
                      UserId leaderUserId, Integer sort, String status) {
        this(id, tenantId, code, name, parentId, leaderUserId, sort, status, null, null, null, null);
    }

    public Department(DepartmentId id, TenantId tenantId, String code, String name, DepartmentId parentId, UserId leaderUserId,
                      Integer sort, String status, String createdBy, Instant createdAt, String updatedBy,
                      Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.leaderUserId = leaderUserId;
        this.sort = sort;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
