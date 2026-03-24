package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;

public record UserIdentityResponse(Long id, Long tenantId, Long userId, String identityType, String identityValue,
                                   boolean enabled) {

    public static UserIdentityResponse from(UserIdentityDTO dto) {
        return new UserIdentityResponse(dto.getId(), dto.getTenantId(), dto.getUserId(), dto.getIdentityType(),
                dto.getIdentityValue(), dto.isEnabled());
    }
}
