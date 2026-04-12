package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 菜单领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Menu {

    /** 菜单主键。 */
    private MenuId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 菜单类型。 */
    private String menuType;
    /** 菜单名称。 */
    private String name;
    /** 父菜单主键，根节点为 null。 */
    private MenuId parentId;
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

    public static Menu create(
            MenuId id,
            TenantId tenantId,
            String menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            Integer sort,
            String permissionCode,
            List<Menu> children) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(menuType, "menuType must not be null");
        Objects.requireNonNull(name, "name must not be null");
        return new Menu(
                id, tenantId, menuType, name, parentId, routePath, componentName, icon, sort, permissionCode, children);
    }

    public static Menu reconstruct(
            MenuId id,
            TenantId tenantId,
            String menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            Integer sort,
            String permissionCode,
            List<Menu> children) {
        return new Menu(
                id, tenantId, menuType, name, parentId, routePath, componentName, icon, sort, permissionCode, children);
    }
}
