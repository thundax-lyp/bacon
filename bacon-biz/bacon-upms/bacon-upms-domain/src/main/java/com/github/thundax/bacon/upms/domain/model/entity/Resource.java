package com.github.thundax.bacon.upms.domain.model.entity;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 资源领域实体。
 */
@Getter
public class Resource {

    /** 资源主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
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
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    public Resource(Long id, Long tenantId, String code, String name, String resourceType,
                    String httpMethod, String uri, String status) {
        this(id, tenantId, code, name, resourceType, httpMethod, uri, status, null, null, null, null);
    }

    public Resource(Long id, Long tenantId, String code, String name, String resourceType, String httpMethod,
                    String uri, String status, String createdBy, LocalDateTime createdAt, String updatedBy,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.resourceType = resourceType;
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
