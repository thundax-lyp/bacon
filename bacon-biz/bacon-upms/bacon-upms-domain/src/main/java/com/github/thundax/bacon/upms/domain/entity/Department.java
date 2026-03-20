package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Department {

    private Long id;
    private Long tenantId;
    private String code;
    private String name;
    private Long parentId;
    private Long leaderUserId;
    private String status;
}
