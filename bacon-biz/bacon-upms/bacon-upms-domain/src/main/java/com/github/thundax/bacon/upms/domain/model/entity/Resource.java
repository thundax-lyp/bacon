package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 资源领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Resource {

    /** 资源主键。 */
    private ResourceId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 资源编码。 */
    private String code;
    /** 资源名称。 */
    private String name;
    /** 资源类型。 */
    private ResourceType resourceType;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 资源 URI。 */
    private String uri;
    /** 资源状态。 */
    private ResourceStatus status;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static Resource create(
            ResourceId id,
            TenantId tenantId,
            String code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Resource(
                id, tenantId, code, name, resourceType, httpMethod, uri, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Resource create(
            ResourceId id,
            TenantId tenantId,
            String code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status) {
        return create(id, tenantId, code, name, resourceType, httpMethod, uri, status, null, null, null, null);
    }

    public static Resource reconstruct(
            ResourceId id,
            TenantId tenantId,
            String code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status,
            String createdBy,
            Instant createdAt,
            String updatedBy,
            Instant updatedAt) {
        return new Resource(
                id, tenantId, code, name, resourceType, httpMethod, uri, status, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static Resource reconstruct(
            ResourceId id,
            TenantId tenantId,
            String code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status) {
        return reconstruct(id, tenantId, code, name, resourceType, httpMethod, uri, status, null, null, null, null);
    }
}
