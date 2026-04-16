package com.github.thundax.bacon.auth.api.response;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionFacadeResponse {

    private String sessionId;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String loginType;
    private String sessionStatus;
    private Instant issuedAt;
    private Instant lastAccessTime;
    private Instant expireAt;

    public static CurrentSessionFacadeResponse from(CurrentSessionDTO dto) {
        if (dto == null) {
            return null;
        }
        return new CurrentSessionFacadeResponse(
                dto.getSessionId(),
                dto.getTenantId(),
                dto.getUserId(),
                dto.getIdentityType(),
                dto.getLoginType(),
                dto.getSessionStatus(),
                dto.getIssuedAt(),
                dto.getLastAccessTime(),
                dto.getExpireAt());
    }
}
