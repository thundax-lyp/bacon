package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 岗位跨服务传输对象。
 */
public class PostDTO {

    /** 岗位主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 岗位编码。 */
    private String code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 岗位状态。 */
    private String status;
}
