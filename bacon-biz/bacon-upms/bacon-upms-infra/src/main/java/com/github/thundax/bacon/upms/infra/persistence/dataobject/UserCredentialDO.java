package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_user_credential")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class UserCredentialDO {

    @TableId(type = IdType.INPUT)
    private UserCredentialId id;

    @TableField("tenant_id")
    private TenantId tenantId;

    @TableField("user_id")
    private UserId userId;

    @TableField("identity_id")
    private UserIdentityId identityId;

    @TableField("credential_type")
    private String credentialType;

    @TableField("factor_level")
    private String factorLevel;

    @TableField("credential_value")
    private String credentialValue;

    @TableField("status")
    private String status;

    @TableField("need_change_password")
    private Boolean needChangePassword;

    @TableField("failed_count")
    private Integer failedCount;

    @TableField("failed_limit")
    private Integer failedLimit;

    @TableField("lock_reason")
    private String lockReason;

    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
