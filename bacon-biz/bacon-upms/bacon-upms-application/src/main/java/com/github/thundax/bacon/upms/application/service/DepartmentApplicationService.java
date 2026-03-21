package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.domain.entity.Department;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DepartmentApplicationService {

    private final DepartmentRepository departmentRepository;

    public DepartmentApplicationService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentDTO getDepartmentById(Long tenantId, Long departmentId) {
        return toDto(departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode) {
        return toDto(departmentRepository.findDepartmentByCode(tenantId, departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return departmentRepository.listDepartmentsByIds(tenantId, departmentIds).stream()
                .map(this::toDto)
                .toList();
    }

    private DepartmentDTO toDto(Department department) {
        return new DepartmentDTO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus());
    }
}
