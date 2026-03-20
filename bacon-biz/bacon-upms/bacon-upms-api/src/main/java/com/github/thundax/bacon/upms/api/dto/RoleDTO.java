package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private String roleType;
    private String dataScopeType;
    private String status;
}
