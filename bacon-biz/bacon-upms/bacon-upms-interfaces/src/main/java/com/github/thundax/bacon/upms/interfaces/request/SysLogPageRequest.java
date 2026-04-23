package com.github.thundax.bacon.upms.interfaces.request;

import com.github.thundax.bacon.common.log.LogEventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
