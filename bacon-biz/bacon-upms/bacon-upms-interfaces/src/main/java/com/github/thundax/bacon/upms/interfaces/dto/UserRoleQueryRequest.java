package com.github.thundax.bacon.upms.interfaces.dto;

public class UserRoleQueryRequest {

    private Long tenantId;
    private Long userId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
