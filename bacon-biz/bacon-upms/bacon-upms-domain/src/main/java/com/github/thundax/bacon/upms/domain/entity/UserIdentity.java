package com.github.thundax.bacon.upms.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserIdentity {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String identityValue;
    private boolean enabled;

    public UserIdentity(Long id, Long tenantId, Long userId, String identityType,
                        String identityValue, boolean enabled) {
        this(id, null, null, null, null, tenantId, userId, identityType, identityValue, enabled);
    }

    public UserIdentity(Long id, String createdBy, LocalDateTime createdAt, String updatedBy,
                        LocalDateTime updatedAt, Long tenantId, Long userId, String identityType,
                        String identityValue, boolean enabled) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.userId = userId;
        this.identityType = identityType;
        this.identityValue = identityValue;
        this.enabled = enabled;
    }
}
