package com.github.thundax.bacon.upms.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户菜单树应用层读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuTreeDTO {

    private Long id;
    private String name;
    private String menuType;
    private Long parentId;
    private String routePath;
    private String componentName;
    private String icon;
    private Integer sort;
    private List<UserMenuTreeDTO> children;
}
