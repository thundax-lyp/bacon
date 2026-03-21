package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final InMemoryUpmsStore upmsStore;

    public PermissionRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public List<Menu> getUserMenuTree(Long tenantId, Long userId) {
        return upmsStore.getUserMenus().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        return upmsStore.getUserPermissions().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
    }

    @Override
    public Set<Long> getUserDepartmentIds(Long tenantId, Long userId) {
        return upmsStore.getUserDepartmentScopes().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
    }

    @Override
    public Set<String> getUserScopeTypes(Long tenantId, Long userId) {
        return upmsStore.getUserScopeTypes().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
    }

    @Override
    public boolean hasAllAccess(Long tenantId, Long userId) {
        return upmsStore.getUserAllAccess().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), false);
    }
}
