package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Instant;
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
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("user_id")
    private Long userId;

    @TableField("identity_id")
    private Long identityId;

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
    private Instant lockedUntil;

    @TableField("expires_at")
    private Instant expiresAt;

    @TableField("last_verified_at")
    private Instant lastVerifiedAt;
}
