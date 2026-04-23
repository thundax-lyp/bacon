package com.github.thundax.bacon.upms.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialGetFacadeRequest {

    private String identityType;
    private String identityValue;
}
