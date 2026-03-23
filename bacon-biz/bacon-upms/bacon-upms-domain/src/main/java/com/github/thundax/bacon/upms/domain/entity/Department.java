package com.github.thundax.bacon.upms.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Department {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String code;
    private String name;
    private Long parentId;
    private Long leaderUserId;
    private String status;

    public Department(Long id, Long tenantId, String code, String name, Long parentId,
                      Long leaderUserId, String status) {
        this(id, null, null, null, null, tenantId, code, name, parentId, leaderUserId, status);
    }

    public Department(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                      Long tenantId, String code, String name, Long parentId, Long leaderUserId,
                      String status) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.parentId = parentId;
        this.leaderUserId = leaderUserId;
        this.status = status;
    }
}
