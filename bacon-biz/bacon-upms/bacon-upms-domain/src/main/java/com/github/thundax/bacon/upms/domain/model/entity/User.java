package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户领域实体。
 */
@Getter
@AllArgsConstructor
public class User {

    /** 用户主键。 */
    private UserId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 用户名称。 */
    private String name;
    /** 头像对象主键。 */
    private StoredObjectId avatarObjectId;
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

    public User(
            Long id,
            Long tenantId,
            String name,
            Long avatarObjectId,
            Long departmentId,
            UserStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        this(
                id == null ? null : UserId.of(id),
                tenantId == null ? null : TenantId.of(tenantId),
                name,
                avatarObjectId == null ? null : StoredObjectId.of(avatarObjectId),
                departmentId == null ? null : DepartmentId.of(departmentId),
                status,
                createdBy,
                createdAt,
                updatedBy,
                updatedAt);
    }
}
