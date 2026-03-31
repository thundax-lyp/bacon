package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository {

    Optional<Department> findDepartmentById(TenantId tenantId, DepartmentId departmentId);

    Optional<Department> findDepartmentByCode(TenantId tenantId, String departmentCode);

    List<Department> listDepartmentsByIds(TenantId tenantId, Set<DepartmentId> departmentIds);

    List<Department> listDepartmentTree(TenantId tenantId);

    Department save(Department department);

    void deleteDepartment(TenantId tenantId, DepartmentId departmentId);

    boolean existsChildDepartment(TenantId tenantId, DepartmentId departmentId);

    boolean existsUserInDepartment(TenantId tenantId, DepartmentId departmentId);
}
