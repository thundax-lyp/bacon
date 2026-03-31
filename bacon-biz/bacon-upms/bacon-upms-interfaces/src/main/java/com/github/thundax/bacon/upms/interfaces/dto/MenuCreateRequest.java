package com.github.thundax.bacon.upms.interfaces.dto;

public record MenuCreateRequest(String tenantNo, String menuType, String name, Long parentId, String routePath,
                                String componentName, String icon, Integer sort, String permissionCode) {
}
