package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 租户分页查询对象。
 */
public class TenantPageQueryDTO {

    /** 业务租户标识。 */
    private Long tenantId;
    /** 租户编码。 */
    private String code;
    /** 租户名称。 */
    private String name;
    /** 租户状态。 */
    private String status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
