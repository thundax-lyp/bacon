package com.github.thundax.bacon.upms.api.dto;

import com.github.thundax.bacon.common.id.domain.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源分页查询对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePageQueryDTO {

    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 资源编码。 */
    private String code;
    /** 资源名称。 */
    private String name;
    /** 资源类型。 */
    private String resourceType;
    /** 资源状态。 */
    private String status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
