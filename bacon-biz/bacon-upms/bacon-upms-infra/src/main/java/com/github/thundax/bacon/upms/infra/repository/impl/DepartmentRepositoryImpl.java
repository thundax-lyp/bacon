package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final UpmsRepositorySupport support;
    private final UserRepositoryImpl userRepository;

    public DepartmentRepositoryImpl(UpmsRepositorySupport support, UserRepositoryImpl userRepository) {
        this.support = support;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Department> findDepartmentById(Long tenantId, Long departmentId) {
        return support.findDepartmentById(tenantId, departmentId);
    }

    @Override
    public Optional<Department> findDepartmentByCode(Long tenantId, String departmentCode) {
        return support.findDepartmentByCode(tenantId, departmentCode);
    }

    @Override
    public List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return support.listDepartmentsByIds(tenantId, departmentIds);
    }

    @Override
    public List<Department> listDepartmentTree(Long tenantId) {
        return support.listDepartmentTree(tenantId);
    }

    @Override
    public Department save(Department department) {
        return support.saveDepartment(department);
    }

    @Override
    public void deleteDepartment(Long tenantId, Long departmentId) {
        support.deleteDepartment(tenantId, departmentId);
    }

    @Override
    public boolean existsChildDepartment(Long tenantId, Long departmentId) {
        return support.existsChildDepartment(tenantId, departmentId);
    }

    @Override
    public boolean existsUserInDepartment(Long tenantId, Long departmentId) {
        return userRepository.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
