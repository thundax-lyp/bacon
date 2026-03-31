package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户跨服务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** 用户主键。 */
    private Long id;
    /** 所属租户编号。 */
    private String tenantNo;
    /** 登录账号。 */
    private String account;
    /** 用户名称。 */
    private String name;
    /** 头像对象主键。 */
    private Long avatarObjectId;
    /** 手机号。 */
    private String phone;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 头像访问地址。 */
    private String avatarUrl;
    /** 用户状态。 */
    private String status;
}
