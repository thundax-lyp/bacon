package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
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

    public DepartmentDTO getDepartmentById(TenantId tenantId, Long departmentId) {
        return toDto(departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentById(String tenantId, Long departmentId) {
        return getDepartmentById(resolveTenantIdByTenantId(tenantId), departmentId);
    }

    public DepartmentDTO getDepartmentByCode(TenantId tenantId, String departmentCode) {
        return toDto(departmentRepository.findDepartmentByCode(tenantId, departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public DepartmentDTO getDepartmentByCode(String tenantId, String departmentCode) {
        return getDepartmentByCode(resolveTenantIdByTenantId(tenantId), departmentCode);
    }

    public List<DepartmentDTO> listDepartmentsByIds(TenantId tenantId, Set<Long> departmentIds) {
        String tenantIdValue = tenantId.value();
        return departmentRepository.listDepartmentsByIds(tenantId, departmentIds).stream()
                .map(department -> toDto(department, tenantIdValue))
                .toList();
    }

    public List<DepartmentDTO> listDepartmentsByIds(String tenantId, Set<Long> departmentIds) {
        return listDepartmentsByIds(resolveTenantIdByTenantId(tenantId), departmentIds);
    }

    public List<DepartmentTreeDTO> getDepartmentTree(TenantId tenantId) {
        List<Department> departments = departmentRepository.listDepartmentTree(tenantId);
        String tenantIdValue = tenantId.value();
        // 先平铺映射成节点表，再按 parentId 二次挂接，避免 repository 被迫返回固定层级结构。
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(department -> toTreeDto(department, tenantIdValue))
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

    public DepartmentDTO createDepartment(TenantId tenantId, String code, String name, Long parentId, String leaderUserId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateParent(tenantId, parentId);
        return toDto(departmentRepository.save(new Department(null, tenantId, normalize(code), normalize(name),
                parentId == null ? 0L : parentId, toUserId(leaderUserId), UpmsStatusEnum.ENABLED.value())));
    }

    public DepartmentDTO updateDepartment(TenantId tenantId, Long departmentId, String code, String name, Long parentId,
                                          String leaderUserId) {
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
                toUserId(leaderUserId),
                currentDepartment.getStatus(),
                currentDepartment.getCreatedBy(),
                currentDepartment.getCreatedAt(),
                currentDepartment.getUpdatedBy(),
                currentDepartment.getUpdatedAt())));
    }

    public void deleteDepartment(TenantId tenantId, Long departmentId) {
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
        return toDto(department, department.getTenantId().value());
    }

    private DepartmentDTO toDto(Department department, String tenantIdValue) {
        return new DepartmentDTO(department.getId(), tenantIdValue,
                department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId() == null ? null : department.getLeaderUserId().value(),
                department.getStatus());
    }

    private DepartmentTreeDTO toTreeDto(Department department) {
        return toTreeDto(department, department.getTenantId().value());
    }

    private DepartmentTreeDTO toTreeDto(Department department, String tenantIdValue) {
        return new DepartmentTreeDTO(department.getId(), tenantIdValue,
                department.getCode(), department.getName(),
                department.getParentId(), department.getLeaderUserId() == null ? null : department.getLeaderUserId().value(),
                department.getStatus(), new java.util.ArrayList<>());
    }

    private UserId toUserId(String userId) {
        return userId == null || userId.isBlank() ? null : UserId.of(userId.trim());
    }

    private void validateParent(TenantId tenantId, Long parentId) {
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

    private TenantId resolveTenantIdByTenantId(String tenantId) {
        validateRequired(tenantId, "tenantId");
        return tenantRepository.findTenantByTenantId(TenantId.of(tenantId))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private String resolveTenantNoByTenantId(TenantId tenantId) {
        return tenantRepository.findTenantById(tenantId)
                .map(tenant -> tenant.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
    }
}
