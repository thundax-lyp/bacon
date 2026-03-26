package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Role {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String code;
    private String name;
    private String roleType;
    private String dataScopeType;
    private String status;

    public Role(Long id, Long tenantId, String code, String name, String roleType,
                String dataScopeType, String status) {
        this(id, null, null, null, null, tenantId, code, name, roleType, dataScopeType, status);
    }

    public Role(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                Long tenantId, String code, String name, String roleType, String dataScopeType,
                String status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.roleType = roleType;
        this.dataScopeType = dataScopeType;
        this.status = status;
    }
}
