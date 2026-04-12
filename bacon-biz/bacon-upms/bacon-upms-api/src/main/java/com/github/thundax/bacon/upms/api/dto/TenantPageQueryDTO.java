package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户分页查询对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantPageQueryDTO {

    /** 租户名称。 */
    private String name;
    /** 租户状态。 */
    private String status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
