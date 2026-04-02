package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;

/**
 * 用户身份标识查询响应对象。
 */
public record UserIdentityResponse(
        /** 身份标识主键。 */
        String id,
        /** 所属租户编号。 */
        String tenantId,
        /** 关联用户主键。 */
        String userId,
        /** 身份标识类型。 */
        String identityType,
        /** 身份标识值。 */
        String identityValue,
        /** 启用标记。 */
        boolean enabled) {

    public static UserIdentityResponse from(UserIdentityDTO dto) {
        return new UserIdentityResponse(dto.getId(), dto.getTenantId(), dto.getUserId(), dto.getIdentityType(),
                dto.getIdentityValue(), dto.isEnabled());
    }
}
