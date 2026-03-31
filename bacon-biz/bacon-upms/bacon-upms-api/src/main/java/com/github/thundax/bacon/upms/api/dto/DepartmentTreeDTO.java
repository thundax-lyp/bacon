package com.github.thundax.bacon.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门树跨服务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeDTO {

    /** 部门主键。 */
    private Long id;
    /** 所属租户编号。 */
    private String tenantNo;
    /** 部门编码。 */
    private String code;
    /** 部门名称。 */
    private String name;
    /** 父部门主键，根部门固定为 0。 */
    private Long parentId;
    /** 部门负责人用户主键。 */
    private String leaderUserId;
    /** 部门状态。 */
    private String status;
    /** 子部门列表。 */
    private List<DepartmentTreeDTO> children;
}
