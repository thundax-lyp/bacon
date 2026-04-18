package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceStatus;
import com.github.thundax.bacon.upms.domain.model.enums.ResourceType;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
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
    /** 资源编码。 */
    private ResourceCode code;
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

    public static Resource create(
            ResourceId id,
            ResourceCode code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new Resource(id, code, name, resourceType, httpMethod, uri, status);
    }

    public static Resource reconstruct(
            ResourceId id,
            ResourceCode code,
            String name,
            ResourceType resourceType,
            String httpMethod,
            String uri,
            ResourceStatus status) {
        return new Resource(id, code, name, resourceType, httpMethod, uri, status);
    }

    public void update(ResourceCode code, String name, ResourceType resourceType, String httpMethod, String uri) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        this.code = code;
        this.name = name;
        this.resourceType = resourceType;
        this.httpMethod = httpMethod;
        this.uri = uri;
    }

    public void enable() {
        this.status = ResourceStatus.ENABLED;
    }

    public void disable() {
        this.status = ResourceStatus.DISABLED;
    }

    public void changeCode(ResourceCode code) {
        Objects.requireNonNull(code, "code must not be null");
        this.code = code;
    }
}
