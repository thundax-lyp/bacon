package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;

public record MenuUpdateCommand(
        MenuId menuId,
        MenuType menuType,
        String name,
        MenuId parentId,
        String routePath,
        String componentName,
        String icon,
        Integer sort,
        String permissionCode) {}
