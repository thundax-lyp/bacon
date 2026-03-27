package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户分页查询对象。
 */
public class UserPageQueryDTO {

    /** 所属租户主键。 */
    private Long tenantId;
    /** 登录账号。 */
    private String account;
    /** 用户名称。 */
    private String name;
    /** 手机号。 */
    private String phone;
    /** 用户状态。 */
    private String status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
