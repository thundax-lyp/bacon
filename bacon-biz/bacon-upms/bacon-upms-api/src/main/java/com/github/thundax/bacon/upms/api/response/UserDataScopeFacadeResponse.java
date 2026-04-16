package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataScopeFacadeResponse {

    private UserDataScopeDTO dataScope;

    public static UserDataScopeFacadeResponse from(UserDataScopeDTO dataScope) {
        return new UserDataScopeFacadeResponse(dataScope);
    }
}
