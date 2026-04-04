package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.DepartmentId;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DepartmentApplicationService {

    private static final DepartmentId ROOT_DEPARTMENT_ID = DepartmentId.of(0L);

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;
    private final Ids ids;

    public DepartmentApplicationService(DepartmentRepository departmentRepository, TenantRepository tenantRepository, Ids ids) {
        this.departmentRepository = departmentRepository;
        this.tenantRepository = tenantRepository;
        this.ids = ids;
    }

    public DepartmentDTO getDepartmentById(TenantId tenantId, DepartmentId departmentId) {
        return toDto(departmentRepository.findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentById(String tenantId, String departmentId) {
        return getDepartmentById(requireExistingTenantId(tenantId), toDepartmentId(departmentId));
    }

    public DepartmentDTO getDepartmentByCode(TenantId tenantId, String departmentCode) {
        return toDto(departmentRepository.findDepartmentByCode(tenantId, departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public DepartmentDTO getDepartmentByCode(String tenantId, String departmentCode) {
        return getDepartmentByCode(requireExistingTenantId(tenantId), departmentCode);
    }

    public List<DepartmentDTO> listDepartmentsByIds(TenantId tenantId, Set<DepartmentId> departmentIds) {
        String tenantIdValue = tenantId.value();
        return departmentRepository.listDepartmentsByIds(tenantId, departmentIds).stream()
                .map(department -> toDto(department, tenantIdValue))
                .toList();
    }

    public List<DepartmentDTO> listDepartmentsByIds(String tenantId, Set<String> departmentIds) {
        return listDepartmentsByIds(requireExistingTenantId(tenantId), toDepartmentIds(departmentIds));
    }

    public List<DepartmentTreeDTO> getDepartmentTree(TenantId tenantId) {
        List<Department> departments = departmentRepository.listDepartmentTree(tenantId);
        String tenantIdValue = tenantId.value();
        // 先平铺映射成节点表，再按 parentId 二次挂接，避免 repository 被迫返回固定层级结构。
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(department -> toTreeDto(department, tenantIdValue))
                .collect(Collectors.toMap(DepartmentTreeDTO::getId, Function.identity()));

        departments.forEach(department -> {
            if (hasParent(department.getParentId())) {
                DepartmentTreeDTO parent = treeNodeMap.get(department.getParentId().value());
                if (parent != null) {
                    parent.getChildren().add(treeNodeMap.get(department.getId().value()));
                }
            }
        });

        // 最终只返回根节点列表；子节点已经在前一步原地挂接完成。
        return departments.stream()
                .filter(department -> !hasParent(department.getParentId()))
                .map(department -> treeNodeMap.get(department.getId().value()))
                .toList();
    }

    @Transactional
    public DepartmentDTO createDepartment(TenantId tenantId, String code, String name, String parentId, String leaderUserId) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        DepartmentId parentDepartmentId = normalizeParentId(parentId);
        validateParent(tenantId, parentDepartmentId);
        return toDto(departmentRepository.save(new Department(ids.departmentId(), tenantId, normalize(code), normalize(name),
                parentDepartmentId, toUserId(leaderUserId), UpmsStatusEnum.ENABLED.value())));
    }

    @Transactional
    public DepartmentDTO updateDepartment(TenantId tenantId, String departmentId, String code, String name, String parentId,
                                          String leaderUserId) {
        DepartmentId targetDepartmentId = toDepartmentId(departmentId);
        Department currentDepartment = departmentRepository.findDepartmentById(tenantId, targetDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        DepartmentId parentDepartmentId = normalizeParentId(parentId);
        validateParent(tenantId, parentDepartmentId);
        if (targetDepartmentId.equals(parentDepartmentId)) {
            throw new IllegalArgumentException("Department parent cannot be self");
        }
        return toDto(departmentRepository.save(new Department(
                currentDepartment.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                parentDepartmentId,
                toUserId(leaderUserId),
                currentDepartment.getStatus(),
                currentDepartment.getCreatedBy(),
                currentDepartment.getCreatedAt(),
                currentDepartment.getUpdatedBy(),
                currentDepartment.getUpdatedAt())));
    }

    @Transactional
    public void deleteDepartment(TenantId tenantId, String departmentId) {
        DepartmentId targetDepartmentId = toDepartmentId(departmentId);
        departmentRepository.findDepartmentById(tenantId, targetDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        if (departmentRepository.existsChildDepartment(tenantId, targetDepartmentId)) {
            throw new IllegalArgumentException("Department has child departments: " + departmentId);
        }
        if (departmentRepository.existsUserInDepartment(tenantId, targetDepartmentId)) {
            throw new IllegalArgumentException("Department has assigned users: " + departmentId);
        }
        departmentRepository.deleteDepartment(tenantId, targetDepartmentId);
    }

    private DepartmentDTO toDto(Department department) {
        return toDto(department, department.getTenantId().value());
    }

    private DepartmentDTO toDto(Department department, String tenantIdValue) {
        return new DepartmentDTO(department.getId().value(), tenantIdValue,
                department.getCode(), department.getName(),
                department.getParentId() == null ? null : department.getParentId().value(),
                department.getLeaderUserId() == null ? null : department.getLeaderUserId().value(),
                department.getStatus());
    }

    private DepartmentTreeDTO toTreeDto(Department department) {
        return toTreeDto(department, department.getTenantId().value());
    }

    private DepartmentTreeDTO toTreeDto(Department department, String tenantIdValue) {
        return new DepartmentTreeDTO(department.getId().value(), tenantIdValue,
                department.getCode(), department.getName(),
                department.getParentId() == null ? null : department.getParentId().value(),
                department.getLeaderUserId() == null ? null : department.getLeaderUserId().value(),
                department.getStatus(), new java.util.ArrayList<>());
    }

    private UserId toUserId(String userId) {
        return userId == null || userId.isBlank() ? null : UserId.of(userId.trim());
    }

    private void validateParent(TenantId tenantId, DepartmentId parentId) {
        if (!hasParent(parentId)) {
            return;
        }
        departmentRepository.findDepartmentById(tenantId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + parentId));
    }

    private DepartmentId normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? ROOT_DEPARTMENT_ID : DepartmentId.of(parseDepartmentId(parentId));
    }

    private boolean hasParent(DepartmentId parentId) {
        return parentId != null && !ROOT_DEPARTMENT_ID.equals(parentId);
    }

    private DepartmentId toDepartmentId(String departmentId) {
        validateRequired(departmentId, "departmentId");
        return DepartmentId.of(parseDepartmentId(departmentId));
    }

    private Set<DepartmentId> toDepartmentIds(Set<String> departmentIds) {
        return departmentIds == null ? Set.of() : departmentIds.stream().map(this::toDepartmentId).collect(Collectors.toSet());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private Long parseDepartmentId(String departmentId) {
        return Long.parseLong(departmentId.trim());
    }

    private TenantId requireExistingTenantId(String tenantId) {
        validateRequired(tenantId, "tenantId");
        return tenantRepository.findTenantByTenantId(TenantId.of(tenantId))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }
}
