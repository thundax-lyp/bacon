package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户身份标识领域实体。
 */
@Getter
public class UserIdentity {

    /** 身份标识主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 关联用户主键。 */
    private Long userId;
    /** 身份标识类型。 */
    private String identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 启用标记。 */
    private boolean enabled;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    public UserIdentity(Long id, Long tenantId, Long userId, String identityType,
                        String identityValue, boolean enabled) {
        this(id, tenantId, userId, identityType, identityValue, enabled, null, null, null, null);
    }

    public UserIdentity(Long id, Long tenantId, Long userId, String identityType,
                        String identityValue, boolean enabled, String createdBy, LocalDateTime createdAt,
                        String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.identityType = identityType;
        this.identityValue = identityValue;
        this.enabled = enabled;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
