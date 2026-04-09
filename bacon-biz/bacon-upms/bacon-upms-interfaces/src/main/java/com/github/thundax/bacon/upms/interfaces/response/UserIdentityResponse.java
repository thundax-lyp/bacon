package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;

/**
 * 用户身份标识查询响应对象。
 */
public record UserIdentityResponse(
        /** 身份标识主键。 */
        Long id,
        /** 所属租户编号。 */
        Long tenantId,
        /** 关联用户主键。 */
        Long userId,
        /** 身份标识类型。 */
        String identityType,
        /** 身份标识值。 */
        String identityValue,
        /** 身份状态。 */
        String status) {

    public static UserIdentityResponse from(UserIdentityDTO dto) {
        return new UserIdentityResponse(
                dto.getId(),
                dto.getTenantId(),
                dto.getUserId(),
                dto.getIdentityType(),
                dto.getIdentityValue(),
                dto.getStatus());
    }
}
