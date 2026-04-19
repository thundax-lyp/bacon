package com.github.thundax.bacon.upms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户身份应用层读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityDTO {

    private Long id;
    private Long userId;
    private String identityType;
    private String identityValue;
    private String status;
}
