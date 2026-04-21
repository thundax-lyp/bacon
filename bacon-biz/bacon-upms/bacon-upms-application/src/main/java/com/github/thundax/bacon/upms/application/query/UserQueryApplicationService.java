package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.storage.api.facade.StoredObjectReadFacade;
import com.github.thundax.bacon.storage.api.request.StoredObjectGetFacadeRequest;
import com.github.thundax.bacon.storage.api.response.StoredObjectFacadeResponse;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.assembler.TenantAssembler;
import com.github.thundax.bacon.upms.application.assembler.UserAssembler;
import com.github.thundax.bacon.upms.application.assembler.UserIdentityAssembler;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.dto.TenantDTO;
import com.github.thundax.bacon.upms.application.dto.UserDTO;
import com.github.thundax.bacon.upms.application.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.application.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.common.core.result.PageResult;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.AvatarStoredObjectNo;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserQueryApplicationService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final StoredObjectReadFacade storedObjectReadFacade;

    public UserQueryApplicationService(
            UserRepository userRepository,
            UserIdentityRepository userIdentityRepository,
            UserCredentialRepository userCredentialRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            StoredObjectReadFacade storedObjectReadFacade) {
        this.userRepository = userRepository;
        this.userIdentityRepository = userIdentityRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.storedObjectReadFacade = storedObjectReadFacade;
    }

    public UserDTO getUserById(UserId userId) {
        User user = requireUser(userId);
        return UserAssembler.toDto(
                user,
                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                resolveAvatarUrl(user.getAvatarStoredObjectNo()));
    }

    public UserIdentityDTO getUserIdentity(UserIdentityType identityType, String identityValue) {
        UserIdentity userIdentity = userIdentityRepository
                .findIdentity(identityType, identityValue)
                .orElseThrow(() -> new NotFoundException("User identity not found"));
        return UserIdentityAssembler.toDto(userIdentity);
    }

    public UserLoginCredentialDTO getUserLoginCredential(UserIdentityType identityType, String identityValue) {
        UserIdentity userIdentity = userIdentityRepository
                .findIdentity(identityType, identityValue)
                .orElseThrow(() -> new NotFoundException("User identity not found"));
        UserCredential passwordCredential = userCredentialRepository
                .findCredentialByUserId(userIdentity.getUserId(), UserCredentialType.PASSWORD)
                .orElseThrow(() -> new NotFoundException("Password credential not found"));
        passwordCredential.assertVerifiable(Instant.now());
        User user = requireUser(userIdentity.getUserId());
        String account = resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT);
        String phone = resolveIdentityValue(user.getId(), UserIdentityType.PHONE);
        return UserIdentityAssembler.toLoginCredentialDto(user, userIdentity, passwordCredential, account, phone);
    }

    public TenantDTO getTenantByTenantId(TenantId tenantId) {
        Tenant tenant = tenantRepository
                .findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId.value()));
        return TenantAssembler.toDto(tenant);
    }

    public PageResult<UserDTO> page(
            String account, String name, String phone, UserStatus status, Integer pageNo, Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResult<>(
                userRepository.page(account, name, phone, status, normalizedPageNo, normalizedPageSize).stream()
                        .map(user -> UserAssembler.toDto(
                                user,
                                resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                                resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                                null))
                        .toList(),
                userRepository.count(account, name, phone, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    public Optional<String> getAvatarAccessUrl(UserId userId) {
        User user = requireUser(userId);
        if (user.getAvatarStoredObjectNo() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resolveAvatarUrl(user.getAvatarStoredObjectNo()));
    }

    public List<RoleDTO> getRolesByUserId(UserId userId) {
        requireUser(userId);
        return roleRepository.findByUserId(userId).stream()
                .map(RoleAssembler::toDto)
                .toList();
    }

    public List<UserDTO> exportUsers(String account, String name, String phone, UserStatus status) {
        return userRepository.list(account, name, phone, status).stream()
                .map(user -> UserAssembler.toDto(
                        user,
                        resolveIdentityValue(user.getId(), UserIdentityType.ACCOUNT),
                        resolveIdentityValue(user.getId(), UserIdentityType.PHONE),
                        null))
                .toList();
    }

    private User requireUser(UserId userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private String resolveIdentityValue(UserId userId, UserIdentityType identityType) {
        return userIdentityRepository
                .findIdentityByUserId(userId, identityType)
                .map(UserIdentity::getIdentityValue)
                .orElse(null);
    }

    private String resolveAvatarUrl(AvatarStoredObjectNo avatarStoredObjectNo) {
        if (avatarStoredObjectNo == null) {
            return null;
        }
        StoredObjectFacadeResponse storedObject =
                storedObjectReadFacade.getObjectByNo(new StoredObjectGetFacadeRequest(avatarStoredObjectNo.value()));
        return storedObject == null ? null : storedObject.getAccessEndpoint();
    }
}
