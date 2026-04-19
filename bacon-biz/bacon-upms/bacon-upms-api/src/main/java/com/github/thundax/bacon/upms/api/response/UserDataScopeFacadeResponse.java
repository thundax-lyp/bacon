package com.github.thundax.bacon.upms.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataScopeFacadeResponse {

    private UserDataScopeDetailFacadeResponse dataScope;

    public static UserDataScopeFacadeResponse from(UserDataScopeDetailFacadeResponse dataScope) {
        return new UserDataScopeFacadeResponse(dataScope);
    }
}
