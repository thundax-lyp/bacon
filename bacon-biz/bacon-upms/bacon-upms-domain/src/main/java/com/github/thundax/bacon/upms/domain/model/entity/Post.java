package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.PostStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.PostId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 岗位领域实体。
 */
@Getter
@AllArgsConstructor
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

    public Post(
            Long id,
            Long tenantId,
            String code,
            String name,
            Long departmentId,
            PostStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        this(
                id == null ? null : PostId.of(id),
                tenantId == null ? null : TenantId.of(tenantId),
                code,
                name,
                departmentId == null ? null : DepartmentId.of(departmentId),
                status,
                createdBy,
                createdAt,
                updatedBy,
                updatedAt);
    }
}
