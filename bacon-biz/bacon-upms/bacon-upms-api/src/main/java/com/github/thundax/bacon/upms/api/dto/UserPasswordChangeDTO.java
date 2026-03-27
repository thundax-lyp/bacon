package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户密码变更对象。
 */
public class UserPasswordChangeDTO {

    /** 旧密码。 */
    private String oldPassword;
    /** 新密码。 */
    private String newPassword;
}
