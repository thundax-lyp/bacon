package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityDTO {

    private Long id;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String identityValue;
    private boolean enabled;
}
