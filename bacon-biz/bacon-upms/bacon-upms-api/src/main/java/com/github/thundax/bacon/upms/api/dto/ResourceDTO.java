package com.github.thundax.bacon.upms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源跨服务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {

    /** 资源主键。 */
    private String id;
    /** 所属租户编号。 */
    private String tenantId;
    /** 资源编码。 */
    private String code;
    /** 资源名称。 */
    private String name;
    /** 资源类型。 */
    private String resourceType;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 资源 URI。 */
    private String uri;
    /** 资源状态。 */
    private String status;
}
