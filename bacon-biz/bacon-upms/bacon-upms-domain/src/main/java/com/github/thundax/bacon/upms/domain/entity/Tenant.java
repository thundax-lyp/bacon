package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tenant {

    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private String status;
}
