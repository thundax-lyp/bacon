package com.github.thundax.bacon.auth.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionResponse {

    private String sessionId;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String loginType;
    private String sessionStatus;
    private Instant issuedAt;
    private Instant lastAccessTime;
    private Instant expireAt;
}
