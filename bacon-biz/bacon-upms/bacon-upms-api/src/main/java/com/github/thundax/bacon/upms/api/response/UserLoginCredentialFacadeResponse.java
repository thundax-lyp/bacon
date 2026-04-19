package com.github.thundax.bacon.upms.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginCredentialFacadeResponse {

    private UserLoginCredentialDetailFacadeResponse userLoginCredential;

    public static UserLoginCredentialFacadeResponse from(UserLoginCredentialDetailFacadeResponse userLoginCredential) {
        return new UserLoginCredentialFacadeResponse(userLoginCredential);
    }
}
