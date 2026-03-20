package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private Long tenantId;
    private String account;
    private String name;
    private String phone;
    private Long departmentId;
    private String status;
    private boolean deleted;
}
