package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.upms.application.command.UserImportCommand;
import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.UserPageResultDTO;
import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService {

    private static final String DISABLED_STATUS = "DISABLED";
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final SessionCommandFacade sessionCommandFacade;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(UserRepository userRepository, RoleRepository roleRepository,
                                  TenantRepository tenantRepository,
                                  SessionCommandFacade sessionCommandFacade,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.sessionCommandFacade = sessionCommandFacade;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO getUserById(Long tenantId, Long userId) {
        return toDto(requireUser(tenantId, userId));
    }

    public UserIdentityDTO getUserIdentity(Long tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, identityType, identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        return new UserIdentityDTO(userIdentity.getId(), userIdentity.getTenantId(), userIdentity.getUserId(),
                userIdentity.getIdentityType(), userIdentity.getIdentityValue(), userIdentity.isEnabled());
    }

    public UserLoginCredentialDTO getUserLoginCredential(Long tenantId, String identityType, String identityValue) {
        UserIdentity userIdentity = userRepository.findUserIdentity(tenantId, identityType, identityValue)
                .orElseThrow(() -> new IllegalArgumentException("User identity not found"));
        User user = userRepository.findUserById(userIdentity.getTenantId(), userIdentity.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userIdentity.getUserId()));
        return new UserLoginCredentialDTO(user.getTenantId(), user.getId(), user.getAccount(), user.getPhone(),
                user.getStatus(), user.isDeleted(), userIdentity.getIdentityType(), userIdentity.getIdentityValue(),
                userIdentity.isEnabled(), user.getPasswordHash());
    }

    public TenantDTO getTenantByTenantId(Long tenantId) {
        Tenant tenant = tenantRepository.findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return new TenantDTO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(), tenant.getStatus());
    }

    public UserPageResultDTO pageUsers(UserPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new UserPageResultDTO(userRepository.pageUsers(query.getTenantId(), query.getAccount(), query.getName(),
                query.getPhone(), query.getStatus(), pageNo, pageSize).stream().map(this::toDto).toList(),
                userRepository.countUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                        query.getStatus()),
                pageNo, pageSize);
    }

    public UserDTO createUser(Long tenantId, String account, String name, String phone, Long departmentId) {
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, null);
        User savedUser = userRepository.save(new User(null, tenantId, normalize(account), normalize(name), normalize(phone),
                null, departmentId, "ENABLED", false));
        return toDto(savedUser);
    }

    public UserDTO updateUser(Long tenantId, Long userId, String account, String name, String phone, Long departmentId) {
        User currentUser = requireUser(tenantId, userId);
        validateRequired(account, "account");
        validateRequired(name, "name");
        ensureAccountUnique(tenantId, account, userId);
        User savedUser = userRepository.save(new User(currentUser.getId(), currentUser.getCreatedBy(), currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(), currentUser.getUpdatedAt(), tenantId, normalize(account), normalize(name),
                normalize(phone), currentUser.getPasswordHash(), departmentId, currentUser.getStatus(), currentUser.isDeleted()));
        return toDto(savedUser);
    }

    public UserDTO updateUserStatus(Long tenantId, Long userId, String status) {
        User currentUser = requireUser(tenantId, userId);
        validateRequired(status, "status");
        User savedUser = userRepository.save(new User(currentUser.getId(), currentUser.getCreatedBy(), currentUser.getCreatedAt(),
                currentUser.getUpdatedBy(), currentUser.getUpdatedAt(), tenantId, currentUser.getAccount(), currentUser.getName(),
                currentUser.getPhone(), currentUser.getPasswordHash(), currentUser.getDepartmentId(), normalize(status),
                currentUser.isDeleted()));
        if (DISABLED_STATUS.equalsIgnoreCase(savedUser.getStatus())) {
            sessionCommandFacade.invalidateUserSessions(tenantId, userId, "USER_DISABLED");
        }
        return toDto(savedUser);
    }

    public void deleteUser(Long tenantId, Long userId) {
        requireUser(tenantId, userId);
        userRepository.deleteUser(tenantId, userId);
        sessionCommandFacade.invalidateUserSessions(tenantId, userId, "USER_DELETED");
    }

    public UserDTO initPassword(Long tenantId, Long userId) {
        requireUser(tenantId, userId);
        User user = userRepository.updatePassword(tenantId, userId, DEFAULT_PASSWORD);
        sessionCommandFacade.invalidateUserSessions(tenantId, userId, "USER_PASSWORD_INITIALIZED");
        return toDto(user);
    }

    public UserDTO resetPassword(Long tenantId, Long userId, String newPassword) {
        requireUser(tenantId, userId);
        validateRequired(newPassword, "newPassword");
        User user = userRepository.updatePassword(tenantId, userId, normalize(newPassword));
        sessionCommandFacade.invalidateUserSessions(tenantId, userId, "USER_PASSWORD_RESET");
        return toDto(user);
    }

    public void changePassword(Long tenantId, Long userId, String oldPassword, String newPassword) {
        User user = requireUser(tenantId, userId);
        validateRequired(oldPassword, "oldPassword");
        validateRequired(newPassword, "newPassword");
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password invalid");
        }
        userRepository.updatePassword(tenantId, userId, normalize(newPassword));
    }

    public List<RoleDTO> assignRoles(Long tenantId, Long userId, List<Long> roleIds) {
        requireUser(tenantId, userId);
        return userRepository.assignRoles(tenantId, userId, roleIds).stream().map(this::toRoleDto).toList();
    }

    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        requireUser(tenantId, userId);
        return roleRepository.findRolesByUserId(tenantId, userId).stream().map(this::toRoleDto).toList();
    }

    public List<UserDTO> importUsers(Long tenantId, List<UserImportCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        return commands.stream()
                .map(command -> createUser(tenantId, command.account(), command.name(), command.phone(),
                        command.departmentId()))
                .toList();
    }

    public List<UserDTO> exportUsers(UserPageQueryDTO query) {
        return userRepository.listUsers(query.getTenantId(), query.getAccount(), query.getName(), query.getPhone(),
                query.getStatus()).stream().map(this::toDto).toList();
    }

    private User requireUser(Long tenantId, Long userId) {
        return userRepository.findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private void ensureAccountUnique(Long tenantId, String account, Long excludedUserId) {
        userRepository.findUserByAccount(tenantId, normalize(account))
                .filter(existingUser -> !existingUser.getId().equals(excludedUserId))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("User account already exists: " + account);
                });
    }

    private UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getTenantId(), user.getAccount(), user.getName(),
                user.getPhone(), user.getDepartmentId(), user.getStatus(), user.isDeleted());
    }

    private RoleDTO toRoleDto(Role role) {
        return new RoleDTO(role.getId(), role.getTenantId(), role.getCode(), role.getName(), role.getRoleType(),
                role.getDataScopeType(), role.getStatus());
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
