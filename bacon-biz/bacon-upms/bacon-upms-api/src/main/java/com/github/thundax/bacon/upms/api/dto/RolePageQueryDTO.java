package com.github.thundax.bacon.upms.api.dto;

import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色分页查询对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePageQueryDTO {

    /** 角色编码。 */
    private String code;
    /** 角色名称。 */
    private String name;
    /** 角色类型。 */
    private RoleType roleType;
    /** 角色状态。 */
    private RoleStatus status;
    /** 页码，从 1 开始。 */
    private Integer pageNo;
    /** 每页大小。 */
    private Integer pageSize;
}
