package com.github.thundax.bacon.upms.api.response;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionCodeFacadeResponse {

    private Set<String> permissionCodes;

    public static UserPermissionCodeFacadeResponse from(Set<String> permissionCodes) {
        return new UserPermissionCodeFacadeResponse(permissionCodes);
    }
}
