package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import java.util.List;

public record UserMenuTreeResponse(Long id, String name, String menuType, Long parentId, String routePath,
                                   String componentName, String icon, Integer sort,
                                   List<UserMenuTreeResponse> children) {

    public static UserMenuTreeResponse from(UserMenuTreeDTO dto) {
        List<UserMenuTreeResponse> childResponses = dto.getChildren() == null
                ? List.of()
                : dto.getChildren().stream().map(UserMenuTreeResponse::from).toList();
        return new UserMenuTreeResponse(dto.getId(), dto.getName(), dto.getMenuType(), dto.getParentId(),
                dto.getRoutePath(), dto.getComponentName(), dto.getIcon(), dto.getSort(), childResponses);
    }
}
