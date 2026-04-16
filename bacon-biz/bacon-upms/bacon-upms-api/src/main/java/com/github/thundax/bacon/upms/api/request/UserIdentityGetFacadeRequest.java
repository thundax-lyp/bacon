package com.github.thundax.bacon.upms.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityGetFacadeRequest {

    private String identityType;
    private String identityValue;
}
