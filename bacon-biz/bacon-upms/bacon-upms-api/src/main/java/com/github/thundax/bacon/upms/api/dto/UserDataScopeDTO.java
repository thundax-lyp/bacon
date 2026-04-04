package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户数据范围传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataScopeDTO {

    /** 是否拥有全部数据权限。 */
    private boolean allAccess;
    /** 数据范围类型集合。 */
    private Set<String> scopeTypes;
    /** 可访问部门主键集合。 */
    private Set<Long> departmentIds;
}
