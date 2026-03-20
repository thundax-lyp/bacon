package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserIdentity {

    private Long id;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String identityValue;
    private boolean enabled;
}
