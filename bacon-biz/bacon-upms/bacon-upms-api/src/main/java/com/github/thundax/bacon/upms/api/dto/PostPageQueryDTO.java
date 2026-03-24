package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPageQueryDTO {

    private Long tenantId;
    private String code;
    private String name;
    private Long departmentId;
    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
