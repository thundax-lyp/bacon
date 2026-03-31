package com.github.thundax.bacon.upms.api.dto;

import com.github.thundax.bacon.common.id.domain.TenantId;
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
    private TenantId id;
    /** 租户名称。 */
    private String name;
    /** 租户状态。 */
    private String status;
}
