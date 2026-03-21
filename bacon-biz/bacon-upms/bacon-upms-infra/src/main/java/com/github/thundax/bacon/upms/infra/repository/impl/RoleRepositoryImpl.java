package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final InMemoryUpmsStore upmsStore;

    public RoleRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return Optional.ofNullable(upmsStore.getRoles().get(InMemoryUpmsStore.roleKey(tenantId, roleId)));
    }

    @Override
    public List<Role> findRolesByUserId(Long tenantId, Long userId) {
        return upmsStore.getUserRoles().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
    }
}
