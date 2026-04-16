package com.github.thundax.bacon.upms.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangeFacadeRequest {

    private Long userId;
    private String oldPassword;
    private String newPassword;
}
