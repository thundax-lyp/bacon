package com.github.thundax.bacon.upms.interfaces.dto;

import com.github.thundax.bacon.common.log.LogEventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogPageRequest {

    private String module;
    private LogEventType eventType;
    private String result;
    private String operatorName;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
