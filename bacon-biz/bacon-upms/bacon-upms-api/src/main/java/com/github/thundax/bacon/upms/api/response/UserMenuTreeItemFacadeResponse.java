package com.github.thundax.bacon.upms.api.response;

import java.util.List;

public record UserMenuTreeItemFacadeResponse(
        Long id,
        String name,
        String menuType,
        Long parentId,
        String routePath,
        String componentName,
        String icon,
        Integer sort,
        List<UserMenuTreeItemFacadeResponse> children) {}
