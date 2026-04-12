package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_role_menu_rel")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class RoleMenuRelDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private TenantId tenantId;

    @TableField("role_id")
    private RoleId roleId;

    @TableField("menu_id")
    private MenuId menuId;
}
