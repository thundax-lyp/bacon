package com.github.thundax.bacon.auth.api.response;

import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationFacadeResponse {

    private boolean valid;
    private Long tenantId;
    private Long userId;
    private String sessionId;
    private Long identityId;
    private String identityType;
    private Instant expireAt;

    public static SessionValidationFacadeResponse from(SessionValidationDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SessionValidationFacadeResponse(
                dto.isValid(),
                dto.getTenantId(),
                dto.getUserId(),
                dto.getSessionId(),
                dto.getIdentityId(),
                dto.getIdentityType(),
                dto.getExpireAt());
    }
}
