package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeDTO {

    private Long id;
    private Long tenantId;
    private String menuType;
    private String name;
    private Long parentId;
    private String routePath;
    private String componentName;
    private String icon;
    private Integer sort;
    private String permissionCode;
    private List<MenuTreeDTO> children;
}
