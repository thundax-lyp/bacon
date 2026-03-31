package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentRepositoryImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldManageDepartmentIndexesAndUserRelationChecks() {
        when(passwordEncoder.encode(anyString())).thenReturn("ENC:123456");
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        UserRepositoryImpl userRepository = new UserRepositoryImpl(roleRepository, passwordEncoder);
        DepartmentRepositoryImpl departmentRepository = new DepartmentRepositoryImpl(userRepository);

        Department rootDepartment = departmentRepository.save(new Department(null, 1L, "ROOT", "Root", 0L, null, "ACTIVE"));
        Department childDepartment = departmentRepository.save(new Department(null, 1L, "SALES", "Sales", rootDepartment.getId(), null, "ACTIVE"));
        userRepository.save(new User(null, 1L, "alice", "Alice", "13800000001", null, childDepartment.getId(), "ACTIVE", false));

        assertThat(departmentRepository.findDepartmentByCode(1L, "SALES")).contains(childDepartment);
        assertThat(departmentRepository.listDepartmentsByIds(1L, Set.of(rootDepartment.getId(), childDepartment.getId())))
                .extracting(Department::getCode)
                .containsExactlyInAnyOrder("ROOT", "SALES");
        assertThat(departmentRepository.existsChildDepartment(1L, rootDepartment.getId())).isTrue();
        assertThat(departmentRepository.existsUserInDepartment(1L, childDepartment.getId())).isTrue();

        departmentRepository.deleteDepartment(1L, childDepartment.getId());

        assertThat(departmentRepository.findDepartmentById(1L, childDepartment.getId())).isEmpty();
    }
}
