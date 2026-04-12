package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;

public final class DepartmentPersistenceAssembler {

    private DepartmentPersistenceAssembler() {}

    public static DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(
                department.getId(),
                department.getTenantId(),
                department.getCode(),
                department.getName(),
                department.getParentId(),
                department.getLeaderUserId(),
                department.getSort(),
                department.getStatus() == null ? null : department.getStatus().value());
    }

    public static Department toDomain(DepartmentDO dataObject) {
        return Department.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                dataObject.getParentId(),
                dataObject.getLeaderUserId(),
                dataObject.getSort(),
                DepartmentStatus.from(dataObject.getStatus()));
    }
}
