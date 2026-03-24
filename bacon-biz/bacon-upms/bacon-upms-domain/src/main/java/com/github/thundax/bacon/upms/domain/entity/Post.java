package com.github.thundax.bacon.upms.domain.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Post {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String code;
    private String name;
    private Long departmentId;
    private String status;

    public Post(Long id, Long tenantId, String code, String name, Long departmentId, String status) {
        this(id, null, null, null, null, tenantId, code, name, departmentId, status);
    }

    public Post(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                Long tenantId, String code, String name, Long departmentId, String status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.status = status;
    }
}
