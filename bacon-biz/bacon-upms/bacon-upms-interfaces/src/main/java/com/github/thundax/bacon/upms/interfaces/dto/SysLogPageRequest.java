package com.github.thundax.bacon.upms.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogPageRequest {

    private String tenantId;
    private String module;
    private String eventType;
    private String result;
    private String operatorName;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
