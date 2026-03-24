package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeDTO {

    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private Long parentId;
    private Long leaderUserId;
    private String status;
    private List<DepartmentTreeDTO> children;
}
