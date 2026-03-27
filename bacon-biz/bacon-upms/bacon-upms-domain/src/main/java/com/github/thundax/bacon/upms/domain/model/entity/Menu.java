package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 菜单领域实体。
 */
@Getter
@AllArgsConstructor
public class Menu {

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
    private List<Menu> children;
}
