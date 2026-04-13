package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository {

    Optional<Department> findDepartmentById(DepartmentId departmentId);

    Optional<Department> findDepartmentByCode(String departmentCode);

    List<Department> listDepartmentsByIds(Set<DepartmentId> departmentIds);

    List<Department> listDepartmentTree();

    Department save(Department department);

    Department updateSort(DepartmentId departmentId, Integer sort);

    void deleteDepartment(DepartmentId departmentId);

    boolean existsChildDepartment(DepartmentId departmentId);

    boolean existsUserInDepartment(DepartmentId departmentId);
}
