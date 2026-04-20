package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.upms.application.assembler.DepartmentAssembler;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentApplicationService {

    private static final String DEPARTMENT_ID_BIZ_TAG = "department-id";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;

    public DepartmentApplicationService(
            DepartmentRepository departmentRepository, UserRepository userRepository, IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
    }

    public DepartmentDTO getDepartmentById(DepartmentId departmentId) {
        return DepartmentAssembler.toDto(departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getByCode(DepartmentCode departmentCode) {
        return DepartmentAssembler.toDto(departmentRepository
                .findByCode(departmentCode)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentCode.value())));
    }

    public List<DepartmentDTO> listByCodes(Set<DepartmentCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return codes.stream()
                .map(departmentRepository::findByCode)
                .flatMap(Optional::stream)
                .map(DepartmentAssembler::toDto)
                .toList();
    }

    public List<DepartmentDTO> listByIds(Set<DepartmentId> departmentIds) {
        return departmentRepository.listByIds(departmentIds).stream()
                .map(DepartmentAssembler::toDto)
                .toList();
    }

    public List<DepartmentTreeDTO> getDepartmentTree() {
        List<Department> departments = departmentRepository.listTree();
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
            DepartmentCode code, String name, DepartmentId parentId, UserId leaderUserId) {
        validateParent(parentId);
        validateLeaderUser(leaderUserId);
        return DepartmentAssembler.toDto(departmentRepository.insert(Department.create(
                DepartmentIdCodec.toDomain(idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG)),
                code,
                name,
                parentId,
                leaderUserId)));
    }

    @Transactional
    public DepartmentDTO updateDepartment(
            DepartmentId departmentId,
            DepartmentCode code,
            String name,
            DepartmentId parentId,
            UserId leaderUserId,
            Integer sort) {
        Department currentDepartment = departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        validateParent(parentId);
        validateLeaderUser(leaderUserId);
        currentDepartment.recodeAs(code);
        currentDepartment.rename(name);
        currentDepartment.moveUnder(parentId);
        currentDepartment.appointLeader(leaderUserId);
        if (sort != null) {
            currentDepartment.sort(sort);
        }
        return DepartmentAssembler.toDto(departmentRepository.update(currentDepartment));
    }

    @Transactional
    public DepartmentDTO updateDepartmentSort(DepartmentId departmentId, Integer sort) {
        if (sort == null) {
            throw new BadRequestException("sort must not be null");
        }
        Department currentDepartment = departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        currentDepartment.sort(sort);
        return DepartmentAssembler.toDto(departmentRepository.update(currentDepartment));
    }

    @Transactional
    public void delete(DepartmentId departmentId) {
        departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
        if (departmentRepository.existsChild(departmentId)) {
            throw new ConflictException("Department has child departments: " + departmentId);
        }
        if (departmentRepository.existsUser(departmentId)) {
            throw new ConflictException("Department has assigned users: " + departmentId);
        }
        departmentRepository.delete(departmentId);
    }

    private Comparator<DepartmentTreeDTO> treeComparator() {
        return Comparator.comparing(DepartmentTreeDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentTreeDTO::getId);
    }

    private void validateParent(DepartmentId parentId) {
        if (!hasParent(parentId)) {
            return;
        }
        departmentRepository
                .findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent department not found: " + parentId));
    }

    private void validateLeaderUser(UserId leaderUserId) {
        if (leaderUserId == null) {
            return;
        }
        userRepository
                .findById(leaderUserId)
                .orElseThrow(() -> new NotFoundException("Leader user not found: " + leaderUserId.value()));
    }

    private boolean hasParent(DepartmentId parentId) {
        return parentId != null;
    }
}
