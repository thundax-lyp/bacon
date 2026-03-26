package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

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
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Department> listDepartmentTree(Long tenantId) {
        return upmsStore.getDepartments().values().stream()
                .filter(department -> department.getTenantId().equals(tenantId))
                .filter(department -> department.getCode() != null)
                .distinct()
                .sorted(java.util.Comparator.comparing(Department::getId))
                .toList();
    }

    @Override
    public Department save(Department department) {
        Department savedDepartment = department.getId() == null
                ? new Department(upmsStore.nextDepartmentId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus())
                : department;
        upmsStore.getDepartments().entrySet().removeIf(entry ->
                entry.getValue().getTenantId().equals(savedDepartment.getTenantId())
                        && entry.getValue().getId().equals(savedDepartment.getId()));
        upmsStore.getDepartments().put(InMemoryUpmsStore.departmentKey(savedDepartment.getTenantId(), savedDepartment.getId()),
                savedDepartment);
        upmsStore.getDepartments().put(InMemoryUpmsStore.departmentCodeKey(savedDepartment.getTenantId(), savedDepartment.getCode()),
                savedDepartment);
        return savedDepartment;
    }

    @Override
    public void deleteDepartment(Long tenantId, Long departmentId) {
        Department department = findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        upmsStore.getDepartments().remove(InMemoryUpmsStore.departmentKey(tenantId, departmentId));
        upmsStore.getDepartments().remove(InMemoryUpmsStore.departmentCodeKey(tenantId, department.getCode()));
    }

    @Override
    public boolean existsChildDepartment(Long tenantId, Long departmentId) {
        return upmsStore.getDepartments().values().stream()
                .filter(department -> department.getTenantId().equals(tenantId))
                .anyMatch(department -> departmentId.equals(department.getParentId()));
    }

    @Override
    public boolean existsUserInDepartment(Long tenantId, Long departmentId) {
        return upmsStore.getUsers().values().stream()
                .filter(user -> user.getTenantId().equals(tenantId))
                .filter(user -> !user.isDeleted())
                .anyMatch(user -> departmentId.equals(user.getDepartmentId()));
    }
}
