package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Role {

    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private String roleType;
    private String dataScopeType;
    private String status;
}
