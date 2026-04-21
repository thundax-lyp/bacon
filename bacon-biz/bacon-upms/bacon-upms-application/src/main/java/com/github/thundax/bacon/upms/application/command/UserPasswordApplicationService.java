package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.application.assembler.UserAssembler;
import com.github.thundax.bacon.upms.application.command.UserPasswordChangeCommand;
import com.github.thundax.bacon.upms.application.command.UserPasswordResetCommand;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPasswordApplicationService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final int PASSWORD_FAILED_LIMIT = 5;
    private static final long PASSWORD_EXPIRE_DAYS = 90L;
    private static final String USER_CREDENTIAL_ID_BIZ_TAG = "user-credential-id";

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;

    public UserPasswordApplicationService(
            UserRepository userRepository,
            UserCredentialRepository userCredentialRepository,
            UserIdentityRepository userIdentityRepository,
            SessionCommandFacade sessionCommandFacade,
            PasswordEncoder passwordEncoder,
            IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public UserDTO initPassword(UserId userId) {
        requireUser(userId);
        User user = userRepository.updatePassword(
                userId,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                true,
                PASSWORD_FAILED_LIMIT,
                passwordExpiresAt(),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), userId.value(), "USER_PASSWORD_INITIALIZED"));
        return toUserDto(user);
    }

    @Transactional
    public UserDTO resetPassword(UserPasswordResetCommand command) {
        requireUser(command.userId());
        validateRequired(command.newPassword(), "newPassword");
        User user = userRepository.updatePassword(
                command.userId(),
                passwordEncoder.encode(command.newPassword().trim()),
                true,
                PASSWORD_FAILED_LIMIT,
                passwordExpiresAt(),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
        sessionCommandFacade.invalidateUserSessions(
                new SessionInvalidateUserFacadeRequest(
                        BaconContextHolder.requireTenantId(), command.userId().value(), "USER_PASSWORD_RESET"));
        return toUserDto(user);
    }

    @Transactional
    public void changePassword(UserPasswordChangeCommand command) {
        requireUser(command.userId());
        UserCredential passwordCredential = userCredentialRepository
                .findCredentialByUserId(command.userId(), UserCredentialType.PASSWORD)
                .orElseThrow(() -> new NotFoundException("Password credential not found: " + command.userId()));
        validateRequired(command.oldPassword(), "oldPassword");
        validateRequired(command.newPassword(), "newPassword");
        if (!passwordEncoder.matches(command.oldPassword(), passwordCredential.getCredentialValue())) {
            throw new BadRequestException("Old password invalid");
        }
        userRepository.updatePassword(
                command.userId(),
                passwordEncoder.encode(command.newPassword().trim()),
                false,
                PASSWORD_FAILED_LIMIT,
                passwordExpiresAt(),
                UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG)));
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private UserDTO toUserDto(User user) {
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                null);
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private Instant passwordExpiresAt() {
        return Instant.now().plus(PASSWORD_EXPIRE_DAYS, ChronoUnit.DAYS);
    }
}
