package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户身份标识跨服务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentityDTO {

    /** 身份标识主键。 */
    private Long id;
    /** 所属租户编号。 */
    private String tenantId;
    /** 关联用户主键。 */
    private String userId;
    /** 身份标识类型。 */
    private String identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 启用标记。 */
    private boolean enabled;
}
