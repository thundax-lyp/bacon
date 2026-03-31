package com.github.thundax.bacon.upms.domain.model.entity;

import java.time.Instant;
import lombok.Getter;

/**
 * 岗位领域实体。
 */
@Getter
public class Post {

    /** 岗位主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 岗位编码。 */
    private String code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 岗位状态。 */
    private String status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public Post(Long id, Long tenantId, String code, String name, Long departmentId, String status) {
        this(id, tenantId, code, name, departmentId, status, null, null, null, null);
    }

    public Post(Long id, Long tenantId, String code, String name, Long departmentId, String status,
                String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
