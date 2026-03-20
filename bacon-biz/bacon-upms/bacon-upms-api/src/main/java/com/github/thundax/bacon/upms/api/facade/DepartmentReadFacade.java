package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import java.util.List;
import java.util.Set;

public interface DepartmentReadFacade {

    DepartmentDTO getDepartmentById(Long tenantId, Long departmentId);

    DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode);

    List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds);
}
