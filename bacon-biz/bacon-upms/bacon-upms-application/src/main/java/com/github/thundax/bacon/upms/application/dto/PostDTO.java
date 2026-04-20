package com.github.thundax.bacon.upms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 岗位内部读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    /** 岗位主键。 */
    private Long id;
    /** 岗位编码。 */
    private String code;
    /** 岗位名称。 */
    private String name;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 岗位状态。 */
    private String status;
}
