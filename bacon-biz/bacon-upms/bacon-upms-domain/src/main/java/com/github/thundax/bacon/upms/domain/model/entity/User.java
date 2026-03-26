package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class User {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private String account;
    private String name;
    private String phone;
    private String passwordHash;
    private Long departmentId;
    private String status;
    private boolean deleted;

    public User(Long id, Long tenantId, String account, String name, String phone, String passwordHash,
                Long departmentId, String status, boolean deleted) {
        this(id, null, null, null, null, tenantId, account, name, phone, passwordHash, departmentId, status, deleted);
    }

    public User(Long id, String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt,
                Long tenantId, String account, String name, String phone, String passwordHash, Long departmentId,
                String status, boolean deleted) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.account = account;
        this.name = name;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.departmentId = departmentId;
        this.status = status;
        this.deleted = deleted;
    }
}
