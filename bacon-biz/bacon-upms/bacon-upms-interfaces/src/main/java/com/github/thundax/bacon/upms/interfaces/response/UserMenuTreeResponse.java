package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import java.util.List;

/**
 * 用户菜单树查询响应对象。
 */
public record UserMenuTreeResponse(
        /** 菜单主键。 */
        String id,
        /** 菜单名称。 */
        String name,
        /** 菜单类型。 */
        String menuType,
        /** 父菜单主键，根节点固定为 0。 */
        String parentId,
        /** 前端路由路径。 */
        String routePath,
        /** 前端组件名称。 */
        String componentName,
        /** 前端图标标识。 */
        String icon,
        /** 排序值。 */
        Integer sort,
        /** 子菜单列表。 */
        List<UserMenuTreeResponse> children) {

    public static UserMenuTreeResponse from(UserMenuTreeDTO dto) {
        List<UserMenuTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(UserMenuTreeResponse::from).toList();
        return new UserMenuTreeResponse(dto.getId(), dto.getName(), dto.getMenuType(), dto.getParentId(),
                dto.getRoutePath(), dto.getComponentName(), dto.getIcon(), dto.getSort(), childResponses);
    }
}
