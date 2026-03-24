package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationDTO {

    private boolean valid;
    private Long tenantId;
    private Long userId;
    private String sessionId;
    private String identityId;
    private String identityType;
    private Instant expireAt;
}
