package com.github.thundax.bacon.upms.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityFacadeResponse {

    private UserIdentityDetailFacadeResponse userIdentity;

    public static UserIdentityFacadeResponse from(UserIdentityDetailFacadeResponse userIdentity) {
        return new UserIdentityFacadeResponse(userIdentity);
    }
}
