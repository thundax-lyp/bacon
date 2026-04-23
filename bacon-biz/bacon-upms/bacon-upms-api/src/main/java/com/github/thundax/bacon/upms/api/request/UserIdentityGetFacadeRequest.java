package com.github.thundax.bacon.upms.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityGetFacadeRequest {

    private String identityType;
    private String identityValue;
}
