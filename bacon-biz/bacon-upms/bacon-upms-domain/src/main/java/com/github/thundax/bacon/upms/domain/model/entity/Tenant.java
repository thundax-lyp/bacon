package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Tenant {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String code;
    private String name;
    private String status;

    public Tenant(Long id, Long tenantId, String code, String name, String status) {
        this(id, null, null, null, null, tenantId, code, name, status);
    }

    public Tenant(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                  Long tenantId, String code, String name, String status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.status = status;
    }
}
