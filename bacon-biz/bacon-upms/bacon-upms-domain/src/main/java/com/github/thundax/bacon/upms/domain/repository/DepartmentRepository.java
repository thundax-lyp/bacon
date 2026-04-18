package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository {

    Optional<Department> findById(DepartmentId departmentId);

    Optional<Department> findByCode(DepartmentCode departmentCode);

    List<Department> listByIds(Set<DepartmentId> departmentIds);

    List<Department> listTree();

    boolean existsChild(DepartmentId departmentId);

    boolean existsUser(DepartmentId departmentId);

    Department insert(Department department);

    Department update(Department department);

    void delete(DepartmentId departmentId);
}
