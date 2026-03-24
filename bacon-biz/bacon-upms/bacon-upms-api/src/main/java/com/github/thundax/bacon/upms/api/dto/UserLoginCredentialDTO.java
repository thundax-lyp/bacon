package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginCredentialDTO {

    private Long tenantId;
    private Long userId;
    private String account;
    private String phone;
    private String status;
    private boolean deleted;
    private String identityType;
    private String identityValue;
    private boolean identityEnabled;
    private String passwordHash;
}
