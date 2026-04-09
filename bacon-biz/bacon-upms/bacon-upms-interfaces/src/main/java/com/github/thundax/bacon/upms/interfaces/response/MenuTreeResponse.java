package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import java.util.List;

/**
 * 菜单树查询响应对象。
 */
public record MenuTreeResponse(
        /** 菜单主键。 */
        Long id,
        /** 所属租户编号。 */
        Long tenantId,
        /** 菜单类型。 */
        String menuType,
        /** 菜单名称。 */
        String name,
        /** 父菜单主键，根节点固定为 0。 */
        Long parentId,
        /** 前端路由路径。 */
        String routePath,
        /** 前端组件名称。 */
        String componentName,
        /** 前端图标标识。 */
        String icon,
        /** 排序值。 */
        Integer sort,
        /** 权限编码。 */
        String permissionCode,
        /** 子菜单列表。 */
        List<MenuTreeResponse> children) {

    public static MenuTreeResponse from(MenuTreeDTO dto) {
        List<MenuTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(MenuTreeResponse::from).toList();
        return new MenuTreeResponse(
                dto.getId(),
                dto.getTenantId(),
                dto.getMenuType(),
                dto.getName(),
                dto.getParentId(),
                dto.getRoutePath(),
                dto.getComponentName(),
                dto.getIcon(),
                dto.getSort(),
                dto.getPermissionCode(),
                childResponses);
    }
}
