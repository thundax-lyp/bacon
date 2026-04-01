package com.github.thundax.bacon.upms.interfaces.dto;

public record MenuCreateRequest(String tenantId, String menuType, String name, String parentId, String routePath,
                                String componentName, String icon, Integer sort, String permissionCode) {
}
