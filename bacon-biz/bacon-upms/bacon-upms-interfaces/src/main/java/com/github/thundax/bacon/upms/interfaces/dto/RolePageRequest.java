package com.github.thundax.bacon.upms.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePageRequest {

    private Long tenantId;
    private String code;
    private String name;
    private String roleType;
    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
