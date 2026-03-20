package com.github.thundax.bacon.upms.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Menu {

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
    private List<Menu> children;
}
