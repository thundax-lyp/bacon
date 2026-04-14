package com.github.thundax.bacon.upms.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuUpdateRequest(
        @NotBlank(message = "menuType must not be blank")
                @Size(max = 32, message = "menuType length must be <= 32")
                String menuType,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
                String name,
        Long parentId,
        @Size(max = 255, message = "routePath length must be <= 255")
        String routePath,
        @Size(max = 255, message = "componentName length must be <= 255")
        String componentName,
        @Size(max = 128, message = "icon length must be <= 128")
        String icon,
        Integer sort,
        @Size(max = 128, message = "permissionCode length must be <= 128")
        String permissionCode) {}
