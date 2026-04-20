package com.github.thundax.bacon.upms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门内部读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    /** 部门主键。 */
    private Long id;
    /** 部门编码。 */
    private String code;
    /** 部门名称。 */
    private String name;
    /** 父部门主键，根部门固定为 0。 */
    private Long parentId;
    /** 部门负责人用户主键。 */
    private Long leaderUserId;
    /** 排序值。 */
    private Integer sort;
    /** 部门状态，可选值：ENABLED、DISABLED。 */
    private String status;
}
