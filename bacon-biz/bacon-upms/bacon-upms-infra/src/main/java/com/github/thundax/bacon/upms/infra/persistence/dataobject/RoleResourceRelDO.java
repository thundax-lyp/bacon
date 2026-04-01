package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_role_resource_rel")
public class RoleResourceRelDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("role_id")
    private RoleId roleId;
    @TableField("resource_id")
    private ResourceId resourceId;
}
