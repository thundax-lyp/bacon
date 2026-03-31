package com.github.thundax.bacon.upms.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePageRequest {

    private String tenantId;
    private String code;
    private String name;
    private UpmsRoleTypeQueryEnum roleType;
    private UpmsStatusQueryEnum status;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
