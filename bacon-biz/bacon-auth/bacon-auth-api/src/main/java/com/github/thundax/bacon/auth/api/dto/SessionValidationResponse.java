package com.github.thundax.bacon.auth.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationResponse {

    private boolean valid;
    private Long tenantId;
    private Long userId;
    private String sessionId;
    private String identityId;
    private String identityType;
    private Instant expireAt;
}
