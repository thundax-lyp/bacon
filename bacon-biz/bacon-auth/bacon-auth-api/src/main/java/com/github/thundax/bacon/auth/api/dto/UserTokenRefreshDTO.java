package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenRefreshDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String sessionId;
}
