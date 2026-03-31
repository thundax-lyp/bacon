package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;

import java.util.List;
import java.util.Set;

public interface DepartmentReadFacade {

    DepartmentDTO getDepartmentById(String tenantId, Long departmentId);

    DepartmentDTO getDepartmentByCode(String tenantId, String departmentCode);

    List<DepartmentDTO> listDepartmentsByIds(String tenantId, Set<Long> departmentIds);
}
