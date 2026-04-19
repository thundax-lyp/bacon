package com.github.thundax.bacon.auth.application.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前会话应用层模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionDTO {

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
