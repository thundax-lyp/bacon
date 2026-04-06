package com.github.thundax.bacon.upms.api.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录凭据传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginCredentialDTO {

    /** 所属租户编号。 */
    private Long tenantId;
    /** 用户主键。 */
    private Long userId;
    /** 身份标识主键。 */
    private Long identityId;
    /** 登录账号。 */
    private String account;
    /** 手机号。 */
    private String phone;
    /** 身份标识类型。 */
    private String identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 身份标识状态。 */
    private String identityStatus;
    /** 凭据主键。 */
    private Long credentialId;
    /** 凭据类型。 */
    private String credentialType;
    /** 凭据状态。 */
    private String credentialStatus;
    /** 是否需要修改密码。 */
    private boolean needChangePassword;
    /** 凭据过期时间。 */
    private Instant credentialExpiresAt;
    /** 锁定截止时间。 */
    private Instant lockedUntil;
    /** 是否需要多因子认证。 */
    private boolean mfaRequired;
    /** 二因子类型列表。 */
    private List<String> secondFactorTypes;
    /** 用户状态。 */
    private String status;
    /** 密码哈希。 */
    private String passwordHash;
}
