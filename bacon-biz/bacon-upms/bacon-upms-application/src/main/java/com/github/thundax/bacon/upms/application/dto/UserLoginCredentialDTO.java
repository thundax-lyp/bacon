package com.github.thundax.bacon.upms.application.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录凭据应用层读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginCredentialDTO {

    private Long userId;
    private Long identityId;
    private String account;
    private String phone;
    private String identityType;
    private String identityValue;
    private String identityStatus;
    private Long credentialId;
    private String credentialType;
    private String credentialStatus;
    private boolean needChangePassword;
    private Instant credentialExpiresAt;
    private Instant lockedUntil;
    private boolean mfaRequired;
    private List<String> secondFactorTypes;
    private String status;
    private String passwordHash;
}
