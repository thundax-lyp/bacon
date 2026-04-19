package com.github.thundax.bacon.upms.application.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据范围应用层读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataScopeDTO {

    private boolean allAccess;
    private Set<String> scopeTypes;
    private Set<Long> departmentIds;
}
