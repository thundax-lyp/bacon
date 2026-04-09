package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;

public interface DepartmentReadFacade {

    DepartmentDTO getDepartmentById(@NonNull TenantId tenantId, @NonNull DepartmentId departmentId);

    DepartmentDTO getDepartmentByCode(@NonNull TenantId tenantId, String departmentCode);

    List<DepartmentDTO> listDepartmentsByIds(@NonNull TenantId tenantId, @NonNull Set<DepartmentId> departmentIds);
}
