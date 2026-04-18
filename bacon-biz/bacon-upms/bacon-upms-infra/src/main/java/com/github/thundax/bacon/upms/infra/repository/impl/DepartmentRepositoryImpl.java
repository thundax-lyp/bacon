package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final DepartmentPersistenceSupport support;
    private final UserRepositoryImpl userRepository;

    public DepartmentRepositoryImpl(DepartmentPersistenceSupport support, UserRepositoryImpl userRepository) {
        this.support = support;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Department> findById(DepartmentId departmentId) {
        return support.findById(departmentId);
    }

    @Override
    public Optional<Department> findByCode(DepartmentCode departmentCode) {
        return support.findByCode(departmentCode);
    }

    @Override
    public List<Department> listByIds(Set<DepartmentId> departmentIds) {
        return support.listByIds(departmentIds);
    }

    @Override
    public List<Department> listDepartmentTree() {
        return support.listDepartmentTree();
    }

    @Override
    public Department insert(Department department) {
        return support.insertDepartment(department);
    }

    @Override
    public Department update(Department department) {
        return support.updateDepartment(department);
    }

    @Override
    public Department updateSort(DepartmentId departmentId, Integer sort) {
        return support.updateDepartmentSort(departmentId, sort);
    }

    @Override
    public void delete(DepartmentId departmentId) {
        support.delete(departmentId);
    }

    @Override
    public boolean existsChildDepartment(DepartmentId departmentId) {
        return support.existsChildDepartment(departmentId);
    }

    @Override
    public boolean existsUserInDepartment(DepartmentId departmentId) {
        return userRepository.hasActiveUserInDepartment(departmentId);
    }
}
