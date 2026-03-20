package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenResponse {

    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
}
