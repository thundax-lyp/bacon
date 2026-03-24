package com.github.thundax.bacon.upms.interfaces.dto;

import java.util.Set;

public class DepartmentBatchQueryRequest {

    private Long tenantId;
    private Set<Long> departmentIds;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Set<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }
}
