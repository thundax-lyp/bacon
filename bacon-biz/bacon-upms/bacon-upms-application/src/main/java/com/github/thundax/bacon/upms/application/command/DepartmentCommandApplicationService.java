package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.assembler.DepartmentAssembler;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentCommandApplicationService {

    private static final String DEPARTMENT_ID_BIZ_TAG = "department-id";

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;

    public DepartmentCommandApplicationService(
            DepartmentRepository departmentRepository, UserRepository userRepository, IdGenerator idGenerator) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public DepartmentDTO create(DepartmentCreateCommand command) {
        validateParent(command.parentId());
        validateLeaderUser(command.leaderUserId());
        return DepartmentAssembler.toDto(departmentRepository.insert(Department.create(
                DepartmentIdCodec.toDomain(idGenerator.nextId(DEPARTMENT_ID_BIZ_TAG)),
                command.code(),
                command.name(),
                command.parentId(),
                command.leaderUserId())));
    }

    @Transactional
    public DepartmentDTO update(DepartmentUpdateCommand command) {
        Department currentDepartment = requireDepartment(command.departmentId());
        validateParent(command.parentId());
        validateLeaderUser(command.leaderUserId());
        currentDepartment.recodeAs(command.code());
        currentDepartment.rename(command.name());
        currentDepartment.moveUnder(command.parentId());
        currentDepartment.appointLeader(command.leaderUserId());
        if (command.sort() != null) {
            currentDepartment.sort(command.sort());
        }
        return DepartmentAssembler.toDto(departmentRepository.update(currentDepartment));
    }

    @Transactional
    public DepartmentDTO updateSort(DepartmentSortUpdateCommand command) {
        if (command.sort() == null) {
            throw new BadRequestException("sort must not be null");
        }
        Department currentDepartment = requireDepartment(command.departmentId());
        currentDepartment.sort(command.sort());
        return DepartmentAssembler.toDto(departmentRepository.update(currentDepartment));
    }

    @Transactional
    public void delete(DepartmentId departmentId) {
        requireDepartment(departmentId);
        if (departmentRepository.existsChild(departmentId)) {
            throw new ConflictException("Department has child departments: " + departmentId);
        }
        if (departmentRepository.existsUser(departmentId)) {
            throw new ConflictException("Department has assigned users: " + departmentId);
        }
        departmentRepository.delete(departmentId);
    }

    private Department requireDepartment(DepartmentId departmentId) {
        return departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));
    }

    private void validateParent(DepartmentId parentId) {
        if (parentId == null) {
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
}
