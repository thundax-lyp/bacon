package com.github.thundax.bacon.upms.api.response;

import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginCredentialFacadeResponse {

    private UserLoginCredentialDTO userLoginCredential;

    public static UserLoginCredentialFacadeResponse from(UserLoginCredentialDTO userLoginCredential) {
        return new UserLoginCredentialFacadeResponse(userLoginCredential);
    }
}
