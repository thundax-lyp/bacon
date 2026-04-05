package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import lombok.Getter;

import java.time.Instant;

/**
 * 用户身份标识领域实体。
 */
@Getter
public class UserIdentity {

    /** 身份标识主键。 */
    private UserIdentityId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 关联用户主键。 */
    private UserId userId;
    /** 身份标识类型。 */
    private UserIdentityType identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 身份状态。 */
    private UserIdentityStatus status;
    /** 创建人。 */
    private Long createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private Long updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public UserIdentity(UserIdentityId id, TenantId tenantId, UserId userId, UserIdentityType identityType,
                        String identityValue, UserIdentityStatus status) {
        this(id, tenantId, userId, identityType, identityValue, status, null, null, null, null);
    }

    public UserIdentity(UserIdentityId id, TenantId tenantId, UserId userId, UserIdentityType identityType,
                        String identityValue, UserIdentityStatus status, Long createdBy, Instant createdAt,
                        Long updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.identityType = identityType;
        this.identityValue = identityValue;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
