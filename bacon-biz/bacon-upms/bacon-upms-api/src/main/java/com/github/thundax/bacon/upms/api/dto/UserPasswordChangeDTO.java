package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户密码变更对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangeDTO {

    /** 旧密码。 */
    private String oldPassword;
    /** 新密码。 */
    private String newPassword;
}
