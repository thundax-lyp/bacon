package com.github.thundax.bacon.upms.application.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
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
import org.mockito.ArgumentCaptor;
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
        lenient().when(idGenerator.nextId("user-credential-id")).thenReturn(10003L);
    }

    @Test
    void shouldInitializePasswordWithEncodedDefaultPassword() {
        User user = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123456")).thenReturn("{bcrypt}123456");
        when(userRepository.updatePassword(
                        eq(UserId.of(101L)),
                        eq("{bcrypt}123456"),
                        eq(true),
                        eq(5),
                        any(Instant.class),
                        eq(com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(10003L))))
                .thenReturn(user);

        BaconContextHolder.set(new BaconContextHolder.BaconContext(1001L, 2001L));
        try {
            UserDTO result = service.initPassword(UserId.of(101L));

            ArgumentCaptor<Instant> passwordExpiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(userRepository).updatePassword(
                    eq(UserId.of(101L)),
                    eq("{bcrypt}123456"),
                    eq(true),
                    eq(5),
                    passwordExpiresAtCaptor.capture(),
                    eq(com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(10003L)));
            assertTrue(passwordExpiresAtCaptor.getValue().isAfter(Instant.now()));
            assertNotNull(result);
        } finally {
            BaconContextHolder.clear();
        }
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExistDuringInitPassword() {
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class, () -> service.initPassword(UserId.of(101L)));
    }

    @Test
    void shouldThrowBadRequestWhenNewPasswordIsBlankDuringResetPassword() {
        User user = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));

        org.junit.jupiter.api.Assertions.assertThrows(
                BadRequestException.class,
                () -> service.resetPassword(new UserPasswordResetCommand(UserId.of(101L), "   ")));
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
        when(passwordEncoder.encode("new-password")).thenReturn("{bcrypt}new-password");

        service.changePassword(new UserPasswordChangeCommand(UserId.of(101L), "old-password", "new-password"));

        ArgumentCaptor<Instant> passwordExpiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(userRepository).updatePassword(
                eq(UserId.of(101L)),
                eq("{bcrypt}new-password"),
                eq(false),
                eq(5),
                passwordExpiresAtCaptor.capture(),
                eq(com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId.of(10003L)));
        assertTrue(passwordExpiresAtCaptor.getValue().isAfter(Instant.now()));
    }

    @Test
    void shouldThrowBadRequestWhenOldPasswordIsInvalidDuringChangePassword() {
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
        when(passwordEncoder.matches("old-password", "{noop}identity")).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(
                BadRequestException.class,
                () -> service.changePassword(new UserPasswordChangeCommand(UserId.of(101L), "old-password", "new-password")));
    }

    @Test
    void shouldThrowNotFoundWhenPasswordCredentialDoesNotExistDuringChangePassword() {
        User user = User.reconstruct(UserId.of(101L), "Alice", null, DEPARTMENT_ID, UserStatus.ACTIVE);
        when(userRepository.findById(UserId.of(101L))).thenReturn(Optional.of(user));
        when(userCredentialRepository.findCredentialByUserId(UserId.of(101L), UserCredentialType.PASSWORD))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> service.changePassword(new UserPasswordChangeCommand(UserId.of(101L), "old-password", "new-password")));
    }
}
