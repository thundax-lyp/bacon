package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.api.dto.UserIdentityDTO;
import com.github.thundax.bacon.upms.api.dto.UserLoginCredentialDTO;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserById(Long tenantId, Long userId) {
        User user = userRepository.findUserById(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return new UserDTO(user.getId(), user.getTenantId(), user.getAccount(), user.getName(),
                user.getPhone(), user.getDepartmentId(), user.getStatus(), user.isDeleted());
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
        Tenant tenant = userRepository.findTenantByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return new TenantDTO(tenant.getId(), tenant.getTenantId(), tenant.getCode(), tenant.getName(), tenant.getStatus());
    }
}
