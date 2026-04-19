package com.github.thundax.bacon.upms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "oldPassword must not be blank")
    private String oldPassword;
    /** 新密码。 */
    @NotBlank(message = "newPassword must not be blank")
    @Size(min = 8, max = 64, message = "newPassword size must be between 8 and 64")
    private String newPassword;
}
