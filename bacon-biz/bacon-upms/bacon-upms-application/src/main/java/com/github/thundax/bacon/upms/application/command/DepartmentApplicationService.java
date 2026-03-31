package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DepartmentApplicationService {

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;

    public DepartmentApplicationService(DepartmentRepository departmentRepository, TenantRepository tenantRepository) {
        this.departmentRepository = departmentRepository;
        this.tenantRepository = tenantRepository;
    }

    public DepartmentDTO getDepartmentById(Long tenantId, Long departmentId) {
        return toDto(departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentById(String tenantNo, Long departmentId) {
        return getDepartmentById(resolveTenantIdByTenantNo(tenantNo), departmentId);
    }

    public DepartmentDTO getDepartmentByCode(Long tenantId, String departmentCode) {
        return toDto(departmentRepository.findDepartmentByCode(tenantId, departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public DepartmentDTO getDepartmentByCode(String tenantNo, String departmentCode) {
        return getDepartmentByCode(resolveTenantIdByTenantNo(tenantNo), departmentCode);
    }

    public List<DepartmentDTO> listDepartmentsByIds(Long tenantId, Set<Long> departmentIds) {
        return departmentRepository.listDepartmentsByIds(tenantId, departmentIds).stream()
                .map(this::toDto)
                .toList();
    }

    public List<DepartmentDTO> listDepartmentsByIds(String tenantNo, Set<Long> departmentIds) {
        return listDepartmentsByIds(resolveTenantIdByTenantNo(tenantNo), departmentIds);
    }

    public List<DepartmentTreeDTO> getDepartmentTree(Long tenantId) {
        List<Department> departments = departmentRepository.listDepartmentTree(tenantId);
        // 先平铺映射成节点表，再按 parentId 二次挂接，避免 repository 被迫返回固定层级结构。
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toMap(DepartmentTreeDTO::getId, Function.identity()));

        departments.forEach(department -> {
            if (department.getParentId() != null && department.getParentId() > 0) {
                DepartmentTreeDTO parent = treeNodeMap.get(department.getParentId());
                if (parent != null) {
                    parent.getChildren().add(treeNodeMap.get(department.getId()));
                }
            }
        });

        // 最终只返回根节点列表；子节点已经在前一步原地挂接完成。
        return departments.stream()
                .filter(department -> department.getParentId() == null || department.getParentId() == 0L)
                .map(department -> treeNodeMap.get(department.getId()))
                .toList();
    }

    public DepartmentDTO createDepartment(Long tenantId, String code, String name, Long parentId, Long leaderUserId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateParent(tenantId, parentId);
        return toDto(departmentRepository.save(new Department(null, tenantId, normalize(code), normalize(name),
                parentId == null ? 0L : parentId, leaderUserId, UpmsStatusEnum.ENABLED.value())));
    }

    public DepartmentDTO updateDepartment(Long tenantId, Long departmentId, String code, String name, Long parentId,
                                          Long leaderUserId) {
        Department currentDepartment = departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateParent(tenantId, parentId);
        if (departmentId.equals(parentId)) {
            throw new IllegalArgumentException("Department parent cannot be self");
        }
        return toDto(departmentRepository.save(new Department(
                currentDepartment.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                parentId == null ? 0L : parentId,
                leaderUserId,
                currentDepartment.getStatus(),
                currentDepartment.getCreatedBy(),
                currentDepartment.getCreatedAt(),
                currentDepartment.getUpdatedBy(),
                currentDepartment.getUpdatedAt())));
    }

    public void deleteDepartment(Long tenantId, Long departmentId) {
        departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        if (departmentRepository.existsChildDepartment(tenantId, departmentId)) {
            throw new IllegalArgumentException("Department has child departments: " + departmentId);
        }
        if (departmentRepository.existsUserInDepartment(tenantId, departmentId)) {
            throw new IllegalArgumentException("Department has assigned users: " + departmentId);
        }
        departmentRepository.deleteDepartment(tenantId, departmentId);
    }

    private DepartmentDTO toDto(Department department) {
        return new DepartmentDTO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus());
    }

    private DepartmentTreeDTO toTreeDto(Department department) {
        return new DepartmentTreeDTO(department.getId(), department.getTenantId(), department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId(), department.getStatus(), new java.util.ArrayList<>());
    }

    private void validateParent(Long tenantId, Long parentId) {
        // 0/NULL 统一视为根节点，避免调用方在“无父节点”语义上出现多套约定。
        if (parentId == null || parentId == 0L) {
            return;
        }
        departmentRepository.findDepartmentById(tenantId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + parentId));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private Long resolveTenantIdByTenantNo(String tenantNo) {
        validateRequired(tenantNo, "tenantNo");
        return tenantRepository.findTenantByTenantNo(normalize(tenantNo))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
    }
}
