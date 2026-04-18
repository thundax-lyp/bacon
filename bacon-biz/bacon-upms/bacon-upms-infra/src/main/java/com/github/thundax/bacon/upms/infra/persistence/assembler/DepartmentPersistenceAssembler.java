package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.DepartmentDO;

public final class DepartmentPersistenceAssembler {

    private DepartmentPersistenceAssembler() {}

    public static DepartmentDO toDataObject(Department department) {
        return new DepartmentDO(
                department.getId() == null ? null : department.getId().value(),
                BaconContextHolder.requireTenantId(),
                department.getCode() == null ? null : department.getCode().value(),
                department.getName(),
                department.getParentId() == null
                        ? null
                        : department.getParentId().value(),
                department.getLeaderUserId() == null
                        ? null
                        : department.getLeaderUserId().value(),
                department.getSort(),
                department.getStatus() == null ? null : department.getStatus().value());
    }

    public static Department toDomain(DepartmentDO dataObject) {
        return Department.reconstruct(
                dataObject.getId() == null ? null : DepartmentId.of(dataObject.getId()),
                dataObject.getCode() == null ? null : DepartmentCode.of(dataObject.getCode()),
                dataObject.getName(),
                dataObject.getParentId() == null ? null : DepartmentId.of(dataObject.getParentId()),
                dataObject.getLeaderUserId() == null ? null : UserId.of(dataObject.getLeaderUserId()),
                dataObject.getSort(),
                DepartmentStatus.from(dataObject.getStatus()));
    }
}
