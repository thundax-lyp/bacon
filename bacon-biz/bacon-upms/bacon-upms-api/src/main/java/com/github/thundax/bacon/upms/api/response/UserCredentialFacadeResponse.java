package com.github.thundax.bacon.upms.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialFacadeResponse {

    private UserCredentialDetailFacadeResponse userCredential;

    public static UserCredentialFacadeResponse from(UserCredentialDetailFacadeResponse userCredential) {
        return new UserCredentialFacadeResponse(userCredential);
    }
}
