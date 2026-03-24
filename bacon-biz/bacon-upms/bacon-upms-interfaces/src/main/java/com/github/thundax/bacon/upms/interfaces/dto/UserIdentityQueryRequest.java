package com.github.thundax.bacon.upms.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityQueryRequest {

    private Long tenantId;
    private String identityType;
    private String identityValue;
}
