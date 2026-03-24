package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import java.util.List;

public record MenuTreeResponse(Long id, Long tenantId, String menuType, String name, Long parentId, String routePath,
                               String componentName, String icon, Integer sort, String permissionCode,
                               List<MenuTreeResponse> children) {

    public static MenuTreeResponse from(MenuTreeDTO dto) {
        List<MenuTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(MenuTreeResponse::from).toList();
        return new MenuTreeResponse(dto.getId(), dto.getTenantId(), dto.getMenuType(), dto.getName(), dto.getParentId(),
                dto.getRoutePath(), dto.getComponentName(), dto.getIcon(), dto.getSort(), dto.getPermissionCode(),
                childResponses);
    }
}
