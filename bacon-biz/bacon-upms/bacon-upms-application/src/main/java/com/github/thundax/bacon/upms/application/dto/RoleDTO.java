package com.github.thundax.bacon.upms.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色内部读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    /** 角色主键。 */
    private Long id;
    /** 角色编码。 */
    private String code;
    /** 角色名称。 */
    private String name;
    /** 角色类型。 */
    private String roleType;
    /** 数据范围类型。 */
    private String dataScopeType;
    /** 角色状态。 */
    private String status;
}
