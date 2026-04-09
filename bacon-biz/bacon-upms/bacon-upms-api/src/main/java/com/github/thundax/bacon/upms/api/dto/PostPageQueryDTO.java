package com.github.thundax.bacon.upms.api.dto;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 岗位分页查询对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPageQueryDTO {

    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 岗位编码。 */
    private String code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private DepartmentId departmentId;
    /** 岗位状态。 */
    private String status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
