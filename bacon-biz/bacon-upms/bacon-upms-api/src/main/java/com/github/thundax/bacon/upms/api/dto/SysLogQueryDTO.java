package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogQueryDTO {

    private String tenantId;
    private String module;
    private String eventType;
    private String result;
    private String operatorName;
    private Integer pageNo;
    private Integer pageSize;
}
