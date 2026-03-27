package com.github.thundax.bacon.upms.api.dto;

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

    /** 所属租户主键。 */
    private Long tenantId;
    /** 用户主键。 */
    private Long userId;
    /** 登录账号。 */
    private String account;
    /** 手机号。 */
    private String phone;
    /** 身份标识类型。 */
    private String identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 身份标识是否启用。 */
    private boolean identityEnabled;
    /** 用户状态。 */
    private String status;
    /** 逻辑删除标记。 */
    private boolean deleted;
    /** 密码哈希。 */
    private String passwordHash;
}
