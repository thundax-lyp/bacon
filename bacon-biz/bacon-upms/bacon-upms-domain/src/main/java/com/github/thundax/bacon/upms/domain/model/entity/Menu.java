package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.upms.domain.exception.MenuErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import java.util.ArrayList;
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
    /** 菜单类型。 */
    private MenuType menuType;
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
            MenuType menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            String permissionCode) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(menuType, "menuType must not be null");
        Objects.requireNonNull(name, "name must not be null");
        validateParentId(id, parentId);
        return new Menu(
                id, menuType, name, parentId, routePath, componentName, icon, 0, permissionCode, new ArrayList<>());
    }

    public static Menu reconstruct(
            MenuId id,
            MenuType menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            Integer sort,
            String permissionCode) {
        return reconstruct(id, menuType, name, parentId, routePath, componentName, icon, sort, permissionCode, List.of());
    }

    public static Menu reconstruct(
            MenuId id,
            MenuType menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            Integer sort,
            String permissionCode,
            List<Menu> children) {
        Objects.requireNonNull(children, "children must not be null");
        return new Menu(
                id,
                menuType,
                name,
                parentId,
                routePath,
                componentName,
                icon,
                sort,
                permissionCode,
                new ArrayList<>(children));
    }

    public void retypeAs(MenuType menuType) {
        Objects.requireNonNull(menuType, "menuType must not be null");
        this.menuType = menuType;
    }

    public void rename(String name) {
        Objects.requireNonNull(name, "name must not be null");
        this.name = name;
    }

    public void moveUnder(MenuId parentId) {
        validateParentId(id, parentId);
        this.parentId = parentId;
    }

    public void routeTo(String routePath) {
        this.routePath = routePath;
    }

    public void renderWith(String componentName) {
        this.componentName = componentName;
    }

    public void showIcon(String icon) {
        this.icon = icon;
    }

    public void bindPermission(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public void sort(Integer sort) {
        if (sort == null || sort < 0) {
            throw new UpmsDomainException(MenuErrorCode.INVALID_MENU_SORT);
        }
        this.sort = sort;
    }

    public void addChild(Menu child) {
        Objects.requireNonNull(child, "child must not be null");
        this.children.add(child);
    }

    public void removeChild(Menu child) {
        Objects.requireNonNull(child, "child must not be null");
        this.children.remove(child);
    }

    private static void validateParentId(MenuId id, MenuId parentId) {
        if (Objects.equals(id, parentId)) {
            throw new UpmsDomainException(MenuErrorCode.MENU_PARENT_CANNOT_BE_SELF);
        }
    }
}
