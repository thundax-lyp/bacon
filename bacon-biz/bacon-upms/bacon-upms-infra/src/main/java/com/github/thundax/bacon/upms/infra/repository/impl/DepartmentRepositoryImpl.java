package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final InMemoryUpmsStore upmsStore;

    public DepartmentRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public Optional<Department> findDepartmentById(Long tenantId, Long departmentId) {
        return Optional.ofNullable(upmsStore.getDepartments().get(InMemoryUpmsStore.departmentKey(tenantId, departmentId)));
    }

    @Override
    public Optional<Department> findDepartmentByCode(Long tenantId, String departmentCode) {
        return Optional.ofNullable(upmsStore.getDepartments().get(InMemoryUpmsStore.departmentCodeKey(tenantId, departmentCode)));
    }

    @Override
    public List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return departmentIds.stream()
                .map(departmentId -> upmsStore.getDepartments().get(InMemoryUpmsStore.departmentKey(tenantId, departmentId)))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
