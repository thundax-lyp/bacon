package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findUserById(Long tenantId, Long userId);

    Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue);

    Optional<Tenant> findTenantByTenantId(Long tenantId);
}
