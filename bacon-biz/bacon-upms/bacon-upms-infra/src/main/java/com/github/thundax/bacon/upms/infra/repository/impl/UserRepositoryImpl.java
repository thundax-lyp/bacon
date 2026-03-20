package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final InMemoryUpmsStore upmsStore;

    public UserRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public Optional<User> findUserById(Long tenantId, Long userId) {
        return Optional.ofNullable(upmsStore.getUsers().get(InMemoryUpmsStore.userKey(tenantId, userId)));
    }

    @Override
    public Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(upmsStore.getUserIdentities()
                .get(InMemoryUpmsStore.identityKey(tenantId, identityType, identityValue)));
    }

    @Override
    public Optional<Tenant> findTenantByTenantId(Long tenantId) {
        return Optional.ofNullable(upmsStore.getTenants().get(tenantId));
    }
}
