package com.github.thundax.bacon.upms.interfaces.dto;

import com.github.thundax.bacon.upms.api.enums.TenantStatusEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantPageRequest {

    private String tenantId;
    private String name;
    private TenantStatusEnum status;

    @Min(1)
    private Integer pageNo;

    @Min(1)
    @Max(200)
    private Integer pageSize;
}
