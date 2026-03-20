package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
