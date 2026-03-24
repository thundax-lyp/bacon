package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageQueryDTO {

    private Long tenantId;
    private String account;
    private String name;
    private String phone;
    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
