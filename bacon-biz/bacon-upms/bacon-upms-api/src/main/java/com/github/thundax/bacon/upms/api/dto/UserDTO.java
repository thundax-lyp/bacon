package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户跨服务传输对象。
 */
public class UserDTO {

    /** 用户主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 登录账号。 */
    private String account;
    /** 用户名称。 */
    private String name;
    /** 手机号。 */
    private String phone;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 用户状态。 */
    private String status;
    /** 逻辑删除标记。 */
    private boolean deleted;
}
