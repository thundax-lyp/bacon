package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;

import java.util.List;
import java.util.Set;

public interface DepartmentReadFacade {

    DepartmentDTO getDepartmentById(String tenantNo, Long departmentId);

    DepartmentDTO getDepartmentByCode(String tenantNo, String departmentCode);

    List<DepartmentDTO> listDepartmentsByIds(String tenantNo, Set<Long> departmentIds);
}
