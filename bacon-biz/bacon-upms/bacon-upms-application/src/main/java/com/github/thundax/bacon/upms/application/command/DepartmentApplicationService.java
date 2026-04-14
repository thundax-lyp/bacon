package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.api.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.application.assembler.DepartmentAssembler;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
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

    public DepartmentDTO getDepartmentById(DepartmentId departmentId) {
        return DepartmentAssembler.toDto(departmentRepository
                .findDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getDepartmentByCode(String departmentCode) {
        return DepartmentAssembler.toDto(departmentRepository
                .findDepartmentByCode(departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentCode)));
    }

    public List<DepartmentDTO> listDepartmentsByIds(Set<DepartmentId> departmentIds) {
        return departmentRepository.listDepartmentsByIds(departmentIds).stream()
                .map(DepartmentAssembler::toDto)
                .toList();
    }

    public List<DepartmentTreeDTO> getDepartmentTree() {
        List<Department> departments = departmentRepository.listDepartmentTree();
        // 先平铺映射成节点表，再按 parentId 二次挂接，避免 repository 被迫返回固定层级结构。
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(DepartmentAssembler::toTreeDto)
                .collect(Collectors.toMap(DepartmentTreeDTO::getId, Function.identity()));

        departments.forEach(department -> {
            if (hasParent(department.getParentId())) {
                DepartmentTreeDTO parent = treeNodeMap.get(DepartmentIdCodec.toValue(department.getParentId()));
                if (parent != null) {
                    parent.getChildren().add(treeNodeMap.get(DepartmentIdCodec.toValue(department.getId())));
                }
            }
        });

        // 最终只返回根节点列表；子节点已经在前一步原地挂接完成。
        return departments.stream()
                .filter(department -> !hasParent(department.getParentId()))
                .map(department -> treeNodeMap.get(DepartmentIdCodec.toValue(department.getId())))
                .sorted(treeComparator())
                .toList();
    }

    @Transactional
    public DepartmentDTO createDepartment(
            String code, String name, DepartmentId parentId, Long leaderUserId, Integer sort) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateParent(parentId);
        return DepartmentAssembler.toDto(departmentRepository.insert(Department.create(
                DepartmentIdCodec.toDomain(idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG)),
                normalize(code),
                normalize(name),
                parentId,
                UserIdCodec.toDomain(leaderUserId),
                defaultSort(sort),
                DepartmentStatus.ENABLED)));
    }

    @Transactional
    public DepartmentDTO updateDepartment(
            DepartmentId departmentId, String code, String name, DepartmentId parentId, Long leaderUserId, Integer sort) {
        Department currentDepartment = departmentRepository
                .findDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateParent(parentId);
        if (departmentId.equals(parentId)) {
            throw new IllegalArgumentException("Department parent cannot be self");
        }
        return DepartmentAssembler.toDto(departmentRepository.update(currentDepartment.update(
                normalize(code),
                normalize(name),
                parentId,
                UserIdCodec.toDomain(leaderUserId),
                sort == null ? currentDepartment.getSort() : sort,
                currentDepartment.getStatus())));
    }

    @Transactional
    public DepartmentDTO updateDepartmentSort(DepartmentId departmentId, Integer sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
        return DepartmentAssembler.toDto(departmentRepository.updateSort(departmentId, sort));
    }

    @Transactional
    public void deleteDepartment(DepartmentId departmentId) {
        departmentRepository
                .findDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
        if (departmentRepository.existsChildDepartment(departmentId)) {
            throw new IllegalArgumentException("Department has child departments: " + departmentId);
        }
        if (departmentRepository.existsUserInDepartment(departmentId)) {
            throw new IllegalArgumentException("Department has assigned users: " + departmentId);
        }
        departmentRepository.deleteDepartment(departmentId);
    }

    private Comparator<DepartmentTreeDTO> treeComparator() {
        return Comparator.comparing(DepartmentTreeDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentTreeDTO::getId);
    }

    private Integer defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private void validateParent(DepartmentId parentId) {
        if (!hasParent(parentId)) {
            return;
        }
        departmentRepository
                .findDepartmentById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent department not found: " + parentId));
    }

    private boolean hasParent(DepartmentId parentId) {
        return parentId != null;
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
