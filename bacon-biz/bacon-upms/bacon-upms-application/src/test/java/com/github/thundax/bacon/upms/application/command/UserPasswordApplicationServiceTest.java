package com.github.thundax.bacon.upms.application.command;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserPasswordApplicationServiceTest {

    private static final DepartmentId DEPARTMENT_ID = DepartmentId.of(11L);

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository userCredentialRepository;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private SessionCommandFacade sessionCommandFacade;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdGenerator idGenerator;

    private UserPasswordApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserPasswordApplicationService(
                userRepository,
                userCredentialRepository,
                userIdentityRepository,
                sessionCommandFacade,
                passwordEncoder,
                idGenerator);
        when(idGenerator.nextId("user-credential-id")).thenReturn(10003L);
    }

    @Test
    void shouldValidateOldPasswordAgainstAccountIdentity() {
        User user = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        UserCredential passwordCredential = UserCredential.createPassword(
                com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(301L),
                UserId.of(101L),
                com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId.of(201L),
                "{noop}identity",
                false,
                5,
                (Instant) null);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.of(passwordCredential));
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(true);

        service.changePassword(new UserPasswordChangeCommand(UserId.of(101L), "old-password", "new-password"));

        verify(userRepository).updatePassword(
                UserId.of(101L),
                "new-password",
                false,
                com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(10003L));
    }
}
