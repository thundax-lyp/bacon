package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepartmentCommandApplicationServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdGenerator idGenerator;

    private DepartmentCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentCommandApplicationService(departmentRepository, userRepository, idGenerator);
    }

    @Test
    void shouldRejectMissingParentDepartment() {
        DepartmentId parentId = DepartmentId.of(11L);
        when(departmentRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () -> service.create(new DepartmentCreateCommand(DepartmentCode.of("OPS"), "Operations", parentId, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Parent department not found: " + parentId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectMissingLeaderUser() {
        when(userRepository.findById(UserId.of(2001L))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        service.create(new DepartmentCreateCommand(
                                DepartmentCode.of("OPS"), "Operations", null, UserId.of(2001L))))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Leader user not found: 2001");
    }

    @Test
    void shouldRejectMissingLeaderUserWhenUpdatingDepartment() {
        DepartmentId departmentId = DepartmentId.of(101L);
        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(department(departmentId, "OPS", "Operations", null, null, 1)));
        when(userRepository.findById(UserId.of(2002L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new DepartmentUpdateCommand(
                        departmentId, DepartmentCode.of("OPS"), "Operations", null, UserId.of(2002L), 2)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Leader user not found: 2002");
    }

    @Test
    void shouldMutateDepartmentBeforePersistingUpdate() {
        DepartmentId departmentId = DepartmentId.of(101L);
        DepartmentId parentId = DepartmentId.of(102L);
        UserId leaderUserId = UserId.of(2002L);
        Department current = department(departmentId, "OPS", "Operations", null, null, 1);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(current));
        when(departmentRepository.findById(parentId)).thenReturn(Optional.of(department(
                parentId, "PARENT", "Parent Department", null, null, 0)));
        when(userRepository.findById(leaderUserId))
                .thenReturn(Optional.of(User.create(leaderUserId, "Leader", null, null)));
        when(departmentRepository.update(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(new DepartmentUpdateCommand(
                departmentId, DepartmentCode.of("OPS-NEW"), "Operations New", parentId, leaderUserId, 2));

        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).update(captor.capture());
        Department updated = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(updated).isSameAs(current);
        org.assertj.core.api.Assertions.assertThat(updated.getCode()).isEqualTo(DepartmentCode.of("OPS-NEW"));
        org.assertj.core.api.Assertions.assertThat(updated.getName()).isEqualTo("Operations New");
        org.assertj.core.api.Assertions.assertThat(updated.getParentId()).isEqualTo(parentId);
        org.assertj.core.api.Assertions.assertThat(updated.getLeaderUserId()).isEqualTo(leaderUserId);
        org.assertj.core.api.Assertions.assertThat(updated.getSort()).isEqualTo(2);
    }

    @Test
    void shouldRejectNullSortWhenUpdatingDepartmentSort() {
        DepartmentId departmentId = DepartmentId.of(101L);
        assertThatThrownBy(() -> service.updateSort(new DepartmentSortUpdateCommand(departmentId, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("sort must not be null");
    }

    @Test
    void shouldDeleteDepartmentWhenNoChildrenOrUsersExist() {
        DepartmentId departmentId = DepartmentId.of(101L);
        when(departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(department(departmentId, "OPS", "Operations", null, null, 1)));
        when(departmentRepository.existsChild(departmentId)).thenReturn(false);
        when(departmentRepository.existsUser(departmentId)).thenReturn(false);

        service.delete(departmentId);

        verify(departmentRepository).delete(departmentId);
    }

    private static Department department(
            DepartmentId id, String code, String name, DepartmentId parentId, UserId leaderUserId, Integer sort) {
        Department department = Department.create(id, DepartmentCode.of(code), name, parentId, leaderUserId);
        department.sort(sort);
        return department;
    }
}
