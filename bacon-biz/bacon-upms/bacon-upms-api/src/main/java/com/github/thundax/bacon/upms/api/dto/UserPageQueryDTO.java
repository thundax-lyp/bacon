package com.github.thundax.bacon.upms.api.dto;

import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户分页查询对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageQueryDTO {

    /** 登录账号。 */
    private String account;
    /** 用户名称。 */
    private String name;
    /** 手机号。 */
    private String phone;
    /** 用户状态。 */
    private UserStatus status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
