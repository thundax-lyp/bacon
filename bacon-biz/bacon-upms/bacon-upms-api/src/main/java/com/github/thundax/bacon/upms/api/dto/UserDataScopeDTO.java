package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataScopeDTO {

    private boolean allAccess;
    private Set<String> scopeTypes;
    private Set<Long> departmentIds;
}
