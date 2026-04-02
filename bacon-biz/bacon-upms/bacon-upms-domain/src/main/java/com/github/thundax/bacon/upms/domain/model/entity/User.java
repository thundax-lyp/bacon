package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import lombok.Getter;

import java.time.Instant;

/**
 * 用户领域实体。
 */
@Getter
public class User {

    /** 用户主键。 */
    private UserId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 用户名称。 */
    private String name;
    /** 头像对象主键。 */
    private Long avatarObjectId;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 用户状态。 */
    private UserStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public User(UserId id, TenantId tenantId, String name, DepartmentId departmentId, UserStatus status) {
        this(id, tenantId, name, null, departmentId, status);
    }

    public User(UserId id, TenantId tenantId, String name, Long avatarObjectId, DepartmentId departmentId, UserStatus status) {
        this(id, tenantId, name, avatarObjectId, departmentId, status,
                null, null, null, null);
    }

    public User(UserId id, TenantId tenantId, String name, DepartmentId departmentId, UserStatus status, String createdBy,
                Instant createdAt,
                String updatedBy, Instant updatedAt) {
        this(id, tenantId, name, null, departmentId, status,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public User(UserId id, TenantId tenantId, String name, Long avatarObjectId, DepartmentId departmentId,
                UserStatus status, String createdBy,
                Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.avatarObjectId = avatarObjectId;
        this.departmentId = departmentId;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
