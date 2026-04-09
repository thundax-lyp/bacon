package com.github.thundax.bacon.upms.interfaces.dto;

public record MenuCreateRequest(
        String tenantCode,
        String menuType,
        String name,
        String parentId,
        String routePath,
        String componentName,
        String icon,
        Integer sort,
        String permissionCode) {}
