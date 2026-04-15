package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.enums.DepartmentStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepartmentApplicationServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdGenerator idGenerator;

    private DepartmentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentApplicationService(departmentRepository, userRepository, idGenerator);
    }

    @Test
    void shouldRejectMissingParentDepartment() {
        DepartmentId parentId = DepartmentId.of(11L);
        when(departmentRepository.findDepartmentById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDepartment(DepartmentCode.of("OPS"), "Operations", parentId, null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parent department not found: " + parentId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectMissingLeaderUser() {
        when(userRepository.findUserById(UserId.of(2001L))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        service.createDepartment(DepartmentCode.of("OPS"), "Operations", null, UserId.of(2001L), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Leader user not found: 2001");
    }

    @Test
    void shouldRejectMissingLeaderUserWhenUpdatingDepartment() {
        DepartmentId departmentId = DepartmentId.of(101L);
        when(departmentRepository.findDepartmentById(departmentId))
                .thenReturn(Optional.of(department(departmentId, "OPS", "Operations", null, null, 1)));
        when(userRepository.findUserById(UserId.of(2002L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDepartment(
                        departmentId, DepartmentCode.of("OPS"), "Operations", null, UserId.of(2002L), 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Leader user not found: 2002");
    }

    private static Department department(
            DepartmentId id, String code, String name, DepartmentId parentId, UserId leaderUserId, Integer sort) {
        return Department.create(id, code, name, parentId, leaderUserId, sort, DepartmentStatus.ENABLED);
    }
}
