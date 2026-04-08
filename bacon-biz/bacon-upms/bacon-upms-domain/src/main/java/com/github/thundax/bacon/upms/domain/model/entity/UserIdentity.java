package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户身份标识领域实体。
 */
@Getter
@AllArgsConstructor
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
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public UserIdentity(Long id, Long tenantId, Long userId, UserIdentityType identityType, String identityValue,
                        UserIdentityStatus status, String createdBy, Instant createdAt, String updatedBy,
                        Instant updatedAt) {
        this(id == null ? null : UserIdentityId.of(id),
                tenantId == null ? null : TenantId.of(tenantId),
                userId == null ? null : UserId.of(userId),
                identityType, identityValue, status, createdBy, createdAt, updatedBy, updatedAt);
    }
}
