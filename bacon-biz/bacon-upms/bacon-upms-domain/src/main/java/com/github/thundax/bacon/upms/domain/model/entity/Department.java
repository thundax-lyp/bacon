package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 部门领域实体。
 */
@Getter
public class Department {

    /** 部门主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 部门编码。 */
    private String code;
    /** 部门名称。 */
    private String name;
    /** 父部门主键，根部门固定为 0。 */
    private Long parentId;
    /** 部门负责人用户主键。 */
    private Long leaderUserId;
    /** 部门状态。 */
    private String status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    public Department(Long id, Long tenantId, String code, String name, Long parentId,
                      Long leaderUserId, String status) {
        this(id, tenantId, code, name, parentId, leaderUserId, status, null, null, null, null);
    }

    public Department(Long id, Long tenantId, String code, String name, Long parentId, Long leaderUserId,
                      String status, String createdBy, LocalDateTime createdAt, String updatedBy,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.leaderUserId = leaderUserId;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
