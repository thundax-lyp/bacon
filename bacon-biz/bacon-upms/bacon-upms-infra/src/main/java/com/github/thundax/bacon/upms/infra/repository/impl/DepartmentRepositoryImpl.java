package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean({DepartmentPersistenceSupport.class, UserPersistenceSupport.class})
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final DepartmentPersistenceSupport support;
    private final UserRepositoryImpl userRepository;

    public DepartmentRepositoryImpl(DepartmentPersistenceSupport support, UserRepositoryImpl userRepository) {
        this.support = support;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Department> findDepartmentById(TenantId tenantId, DepartmentId departmentId) {
        return support.findDepartmentById(tenantId, departmentId);
    }

    @Override
    public Optional<Department> findDepartmentByCode(TenantId tenantId, String departmentCode) {
        return support.findDepartmentByCode(tenantId, departmentCode);
    }

    @Override
    public List<Department> listDepartmentsByIds(TenantId tenantId, Set<DepartmentId> departmentIds) {
        return support.listDepartmentsByIds(tenantId, departmentIds);
    }

    @Override
    public List<Department> listDepartmentTree(TenantId tenantId) {
        return support.listDepartmentTree(tenantId);
    }

    @Override
    public Department save(Department department) {
        return support.saveDepartment(department);
    }

    @Override
    public void deleteDepartment(TenantId tenantId, DepartmentId departmentId) {
        support.deleteDepartment(tenantId, departmentId);
    }

    @Override
    public boolean existsChildDepartment(TenantId tenantId, DepartmentId departmentId) {
        return support.existsChildDepartment(tenantId, departmentId);
    }

    @Override
    public boolean existsUserInDepartment(TenantId tenantId, DepartmentId departmentId) {
        return userRepository.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
