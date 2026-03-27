package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 菜单树跨服务传输对象。
 */
public class MenuTreeDTO {

    /** 菜单主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 菜单类型。 */
    private String menuType;
    /** 菜单名称。 */
    private String name;
    /** 父菜单主键，根节点固定为 0。 */
    private Long parentId;
    /** 前端路由路径。 */
    private String routePath;
    /** 前端组件名称。 */
    private String componentName;
    /** 前端图标标识。 */
    private String icon;
    /** 排序值。 */
    private Integer sort;
    /** 权限编码。 */
    private String permissionCode;
    /** 子菜单列表。 */
    private List<MenuTreeDTO> children;
}
