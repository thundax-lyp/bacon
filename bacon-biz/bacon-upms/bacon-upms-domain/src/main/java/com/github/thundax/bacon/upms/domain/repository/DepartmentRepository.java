package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Department;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository {

    Optional<Department> findDepartmentById(Long tenantId, Long departmentId);

    Optional<Department> findDepartmentByCode(Long tenantId, String departmentCode);

    List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds);
}
