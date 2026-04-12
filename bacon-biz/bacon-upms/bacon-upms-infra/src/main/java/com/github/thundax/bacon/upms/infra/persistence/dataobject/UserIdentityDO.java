package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_user_identity")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class UserIdentityDO {

    @TableId(type = IdType.INPUT)
    private UserIdentityId id;

    @TableField("tenant_id")
    private TenantId tenantId;

    @TableField("user_id")
    private UserId userId;

    @TableField("identity_type")
    private String identityType;

    @TableField("identity_value")
    private String identityValue;

    @TableField("status")
    private String status;
}
