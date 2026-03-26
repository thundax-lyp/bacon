package com.github.thundax.bacon.upms.domain.model.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Resource {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String code;
    private String name;
    private String resourceType;
    private String httpMethod;
    private String uri;
    private String status;

    public Resource(Long id, Long tenantId, String code, String name, String resourceType,
                    String httpMethod, String uri, String status) {
        this(id, null, null, null, null, tenantId, code, name, resourceType, httpMethod, uri, status);
    }

    public Resource(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                    Long tenantId, String code, String name, String resourceType, String httpMethod,
                    String uri, String status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.resourceType = resourceType;
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.status = status;
    }
}
