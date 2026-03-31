package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final Map<String, Department> departmentsById = new ConcurrentHashMap<>();
    private final Map<String, Department> departmentsByCode = new ConcurrentHashMap<>();
    private final AtomicLong departmentIdSequence = new AtomicLong(11L);
    private final UserRepositoryImpl userRepository;

    public DepartmentRepositoryImpl(UserRepositoryImpl userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Department> findDepartmentById(Long tenantId, Long departmentId) {
        return Optional.ofNullable(departmentsById.get(UpmsRepositoryHelper.departmentKey(tenantId, departmentId)));
    }

    @Override
    public Optional<Department> findDepartmentByCode(Long tenantId, String departmentCode) {
        return Optional.ofNullable(departmentsByCode.get(UpmsRepositoryHelper.departmentCodeKey(tenantId, departmentCode)));
    }

    @Override
    public List<Department> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return departmentIds.stream()
                .map(departmentId -> departmentsById.get(UpmsRepositoryHelper.departmentKey(tenantId, departmentId)))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Department> listDepartmentTree(Long tenantId) {
        return departmentsById.values().stream()
                .filter(department -> department.getTenantId().equals(tenantId))
                .filter(department -> department.getCode() != null)
                .sorted(Comparator.comparing(Department::getId))
                .toList();
    }

    @Override
    public Department save(Department department) {
        Department savedDepartment = department.getId() == null
                ? new Department(departmentIdSequence.getAndIncrement(), department.getTenantId(), department.getCode(),
                department.getName(), department.getParentId(), department.getLeaderUserId(), department.getStatus())
                : department;
        departmentsById.put(UpmsRepositoryHelper.departmentKey(savedDepartment.getTenantId(), savedDepartment.getId()), savedDepartment);
        departmentsByCode.entrySet().removeIf(entry -> entry.getValue().getTenantId().equals(savedDepartment.getTenantId())
                && entry.getValue().getId().equals(savedDepartment.getId()));
        departmentsByCode.put(UpmsRepositoryHelper.departmentCodeKey(savedDepartment.getTenantId(), savedDepartment.getCode()), savedDepartment);
        return savedDepartment;
    }

    @Override
    public void deleteDepartment(Long tenantId, Long departmentId) {
        Department department = findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        departmentsById.remove(UpmsRepositoryHelper.departmentKey(tenantId, departmentId));
        departmentsByCode.remove(UpmsRepositoryHelper.departmentCodeKey(tenantId, department.getCode()));
    }

    @Override
    public boolean existsChildDepartment(Long tenantId, Long departmentId) {
        return departmentsById.values().stream()
                .filter(department -> department.getTenantId().equals(tenantId))
                .anyMatch(department -> departmentId.equals(department.getParentId()));
    }

    @Override
    public boolean existsUserInDepartment(Long tenantId, Long departmentId) {
        return userRepository.hasActiveUserInDepartment(tenantId, departmentId);
    }
}
