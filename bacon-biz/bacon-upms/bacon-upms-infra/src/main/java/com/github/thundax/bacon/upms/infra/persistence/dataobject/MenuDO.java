package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.MenuId;
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
@TableName("bacon_upms_menu")
public class MenuDO {

    @TableId(type = IdType.INPUT)
    private MenuId id;
    @TableField("tenant_id")
    private TenantId tenantId;
    @TableField("menu_type")
    private String menuType;
    @TableField("name")
    private String name;
    @TableField("parent_id")
    private MenuId parentId;
    @TableField("route_path")
    private String routePath;
    @TableField("component_name")
    private String componentName;
    @TableField("icon")
    private String icon;
    @TableField("sort")
    private Integer sort;
    @TableField("permission_code")
    private String permissionCode;
}
