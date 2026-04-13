package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.api.enums.EnableStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentApplicationService {

    private static final String DEPARTMENT_ID_BIZ_TAG = "department-id";

    private final DepartmentRepository departmentRepository;
    private final IdGenerator idGenerator;

    public DepartmentApplicationService(DepartmentRepository departmentRepository, IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.idGenerator = idGenerator;
    }

    public DepartmentDTO getDepartmentById(TenantId tenantId, DepartmentId departmentId) {
        return toDto(departmentRepository
                .findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentByCode(TenantId tenantId, String departmentCode) {
        return toDto(departmentRepository
                .findDepartmentByCode(tenantId, departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public List<DepartmentDTO> listDepartmentsByIds(TenantId tenantId, Set<DepartmentId> departmentIds) {
        return departmentRepository.listDepartmentsByIds(tenantId, departmentIds).stream()
                .map(department -> toDto(department, tenantId.value()))
                .toList();
    }

    public List<DepartmentTreeDTO> getDepartmentTree(TenantId tenantId) {
        List<Department> departments = departmentRepository.listDepartmentTree(tenantId);
        // 先平铺映射成节点表，再按 parentId 二次挂接，避免 repository 被迫返回固定层级结构。
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(department -> toTreeDto(department, tenantId.value()))
                .collect(Collectors.toMap(DepartmentTreeDTO::getId, Function.identity()));

        departments.forEach(department -> {
            if (hasParent(department.getParentId())) {
                DepartmentTreeDTO parent =
                        treeNodeMap.get(department.getParentId().value());
                if (parent != null) {
                    parent.getChildren().add(treeNodeMap.get(department.getId().value()));
                }
            }
        });

        // 最终只返回根节点列表；子节点已经在前一步原地挂接完成。
        return departments.stream()
                .filter(department -> !hasParent(department.getParentId()))
                .map(department -> treeNodeMap.get(department.getId().value()))
                .sorted(treeComparator())
                .toList();
    }

    @Transactional
    public DepartmentDTO createDepartment(
            TenantId tenantId, String code, String name, String parentId, String leaderUserId, Integer sort) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        DepartmentId parentDepartmentId = normalizeParentId(parentId);
        validateParent(tenantId, parentDepartmentId);
        UserId leaderId = toUserId(leaderUserId);
        return toDto(departmentRepository.save(Department.create(
                DepartmentId.of(idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG)),
                tenantId,
                normalize(code),
                normalize(name),
                parentDepartmentId,
                leaderId,
                defaultSort(sort),
                DepartmentStatus.ENABLED)));
    }

    @Transactional
    public DepartmentDTO updateDepartment(
            TenantId tenantId,
            DepartmentId departmentId,
            String code,
            String name,
            String parentId,
            String leaderUserId,
            Integer sort) {
        Department currentDepartment = departmentRepository
                .findDepartmentById(tenantId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        DepartmentId parentDepartmentId = normalizeParentId(parentId);
        validateParent(tenantId, parentDepartmentId);
        if (departmentId.equals(parentDepartmentId)) {
            throw new IllegalArgumentException("Department parent cannot be self");
        }
        return toDto(departmentRepository.save(Department.reconstruct(
                currentDepartment.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                parentDepartmentId,
                toUserId(leaderUserId),
                sort == null ? currentDepartment.getSort() : sort,
                currentDepartment.getStatus())));
    }

    @Transactional
    public DepartmentDTO updateDepartmentSort(TenantId tenantId, DepartmentId departmentId, Integer sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
        return toDto(departmentRepository.updateSort(tenantId, departmentId, sort));
    }

    @Transactional
    public void deleteDepartment(TenantId tenantId, DepartmentId departmentId) {
        departmentRepository
                .findDepartmentById(tenantId, departmentId)
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

    private DepartmentDTO toDto(Department department, Long tenantIdValue) {
        return new DepartmentDTO(
                department.getId().value(),
                tenantIdValue,
                department.getCode(),
                department.getName(),
                department.getParentId() == null
                        ? null
                        : department.getParentId().value(),
                department.getLeaderUserId() == null
                        ? null
                        : department.getLeaderUserId().value(),
                department.getSort(),
                toStatusEnum(department.getStatus()));
    }

    private DepartmentTreeDTO toTreeDto(Department department) {
        return toTreeDto(department, department.getTenantId().value());
    }

    private DepartmentTreeDTO toTreeDto(Department department, Long tenantIdValue) {
        return new DepartmentTreeDTO(
                department.getId().value(),
                tenantIdValue,
                department.getCode(),
                department.getName(),
                department.getParentId() == null
                        ? null
                        : department.getParentId().value(),
                department.getLeaderUserId() == null
                        ? null
                        : department.getLeaderUserId().value(),
                department.getSort(),
                toStatusEnum(department.getStatus()),
                new java.util.ArrayList<>());
    }

    private Comparator<DepartmentTreeDTO> treeComparator() {
        return Comparator.comparing(DepartmentTreeDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentTreeDTO::getId);
    }

    private Integer defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private EnableStatusEnum toStatusEnum(DepartmentStatus status) {
        return status == null ? null : EnableStatusEnum.valueOf(status.name());
    }

    private UserId toUserId(String userId) {
        return userId == null || userId.isBlank() ? null : UserIdCodec.toDomain(Long.valueOf(userId.trim()));
    }

    private void validateParent(TenantId tenantId, DepartmentId parentId) {
        if (!hasParent(parentId)) {
            return;
        }
        departmentRepository
                .findDepartmentById(tenantId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + parentId));
    }

    private DepartmentId normalizeParentId(String parentId) {
        return parentId == null || parentId.isBlank() ? null : DepartmentId.of(parseDepartmentId(parentId));
    }

    private boolean hasParent(DepartmentId parentId) {
        return parentId != null;
    }

    private DepartmentId toDepartmentId(String departmentId) {
        validateRequired(departmentId, "departmentId");
        return DepartmentId.of(parseDepartmentId(departmentId));
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
}
