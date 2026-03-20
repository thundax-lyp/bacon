package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {

    private Long id;
    private Long tenantId;
    private String account;
    private String name;
    private String phone;
    private Long departmentId;
    private String status;
    private boolean deleted;
}
