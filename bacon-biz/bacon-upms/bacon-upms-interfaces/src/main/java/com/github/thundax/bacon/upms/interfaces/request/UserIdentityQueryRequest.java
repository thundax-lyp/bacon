package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityQueryRequest {

    @NotBlank(message = "identityType must not be blank")
    private String identityType;

    @NotBlank(message = "identityValue must not be blank")
    private String identityValue;
}
