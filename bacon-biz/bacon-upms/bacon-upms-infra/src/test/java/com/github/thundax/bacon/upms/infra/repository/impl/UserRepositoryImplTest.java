package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldSaveFilterAndUpdateUsers() {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "ENC:" + invocation.getArgument(0));
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        UserRepositoryImpl userRepository = new UserRepositoryImpl(roleRepository, passwordEncoder);

        User savedAlice = userRepository.save(new User(null, 1L, "alice", "Alice", "13800000001", null, 11L, "ACTIVE", false));
        User savedBob = userRepository.save(new User(null, 1L, "bob", "Bob", "13800000002", null, 12L, "DISABLED", false));
        userRepository.save(new User(null, 2L, "tenant2", "Tenant2", "13800000003", null, 21L, "ACTIVE", false));

        assertThat(savedAlice.getPasswordHash()).isEqualTo("ENC:123456");
        assertThat(userRepository.findUserById(1L, savedAlice.getId())).contains(savedAlice);
        assertThat(userRepository.findUserIdentity(1L, "ACCOUNT", "alice")).isPresent();
        assertThat(userRepository.findUserIdentity(1L, "PHONE", "13800000001")).isPresent();

        assertThat(userRepository.pageUsers(1L, null, null, null, null, 1, 1))
                .extracting(User::getAccount)
                .containsExactly("alice");
        assertThat(userRepository.countUsers(1L, null, null, null, null)).isEqualTo(2);
        assertThat(userRepository.listUsers(1L, null, null, "13800000002", "DISABLED"))
                .extracting(User::getAccount)
                .containsExactly("bob");

        User updated = userRepository.updatePassword(1L, savedBob.getId(), "new-password");
        assertThat(updated.getPasswordHash()).isEqualTo("ENC:new-password");
    }

    @Test
    void shouldAssignAndClearUserRolesOnDelete() {
        when(passwordEncoder.encode(anyString())).thenReturn("ENC:123456");
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        UserRepositoryImpl userRepository = new UserRepositoryImpl(roleRepository, passwordEncoder);
        Role adminRole = roleRepository.save(new Role(null, 1L, "ADMIN", "Admin", "SYSTEM", "ALL", "ACTIVE"));
        User savedUser = userRepository.save(new User(null, 1L, "operator", "Operator", "13800000009", null, 11L, "ACTIVE", false));

        List<Role> assignedRoles = userRepository.assignRoles(1L, savedUser.getId(), List.of(adminRole.getId()));

        assertThat(assignedRoles).extracting(Role::getCode).containsExactly("ADMIN");
        assertThat(roleRepository.findRolesByUserId(1L, savedUser.getId()))
                .extracting(Role::getId)
                .containsExactly(adminRole.getId());

        userRepository.deleteUser(1L, savedUser.getId());

        assertThat(userRepository.findUserById(1L, savedUser.getId())).isEmpty();
        assertThat(roleRepository.findRolesByUserId(1L, savedUser.getId())).isEmpty();
        assertThat(userRepository.findUserIdentity(1L, "ACCOUNT", "operator")).isEmpty();
    }
}
