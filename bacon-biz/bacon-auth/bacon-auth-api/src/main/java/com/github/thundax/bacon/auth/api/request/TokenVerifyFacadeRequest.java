package com.github.thundax.bacon.auth.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerifyFacadeRequest {

    private String accessToken;
}
