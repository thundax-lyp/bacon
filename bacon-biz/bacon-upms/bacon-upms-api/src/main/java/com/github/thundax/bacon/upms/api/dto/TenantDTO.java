package com.github.thundax.bacon.upms.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户跨服务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {

    /** 租户主键。 */
    private Long id;
    /** 租户名称。 */
    private String name;
    /** 稳定业务编码。 */
    private String tenantCode;
    /** 租户状态。 */
    private String status;
    /** 过期时间。 */
    private Instant expiredAt;
}
