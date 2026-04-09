package com.github.thundax.bacon.upms.infra.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class UpmsPermissionCacheSupport {

    private static final int DEFAULT_EXPIRE_SECONDS = 300;
    private static final long INITIAL_VERSION = 0L;

    @CreateCache(name = "upms:permission:tenantVersion:", cacheType = CacheType.REMOTE,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<TenantId, Long> tenantPermissionVersionCache;

    @CreateCache(name = "upms:permission:userVersion:", cacheType = CacheType.REMOTE,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, Long> userPermissionVersionCache;

    @CreateCache(name = "upms:permission:tenantMenuTree:", cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, List<Menu>> tenantMenuTreeCache;

    @CreateCache(name = "upms:permission:userMenuTree:", cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, List<Menu>> userMenuTreeCache;

    @CreateCache(name = "upms:permission:userCodes:", cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> userPermissionCodeCache;

    @CreateCache(name = "upms:permission:userDepartments:", cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<DepartmentId>> userDepartmentIdsCache;

    @CreateCache(name = "upms:permission:userScopes:", cacheType = CacheType.BOTH,
            expire = DEFAULT_EXPIRE_SECONDS, timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> userScopeTypesCache;

    public UpmsPermissionCacheSupport() {
    }

    public UpmsPermissionCacheSupport(Cache<TenantId, Long> tenantPermissionVersionCache,
                                      Cache<String, Long> userPermissionVersionCache,
                                      Cache<String, List<Menu>> tenantMenuTreeCache,
                                      Cache<String, List<Menu>> userMenuTreeCache,
                                      Cache<String, Set<String>> userPermissionCodeCache,
                                      Cache<String, Set<DepartmentId>> userDepartmentIdsCache,
                                      Cache<String, Set<String>> userScopeTypesCache) {
        this.tenantPermissionVersionCache = tenantPermissionVersionCache;
        this.userPermissionVersionCache = userPermissionVersionCache;
        this.tenantMenuTreeCache = tenantMenuTreeCache;
        this.userMenuTreeCache = userMenuTreeCache;
        this.userPermissionCodeCache = userPermissionCodeCache;
        this.userDepartmentIdsCache = userDepartmentIdsCache;
        this.userScopeTypesCache = userScopeTypesCache;
    }

    public List<Menu> getTenantMenuTree(TenantId tenantId, Supplier<List<Menu>> loader) {
        String cacheKey = tenantId + ":" + getTenantPermissionVersion(tenantId);
        List<Menu> cachedMenus = tenantMenuTreeCache.get(cacheKey);
        if (cachedMenus != null) {
            return copyMenus(cachedMenus);
        }
        List<Menu> loadedMenus = copyMenus(loader.get());
        tenantMenuTreeCache.put(cacheKey, loadedMenus);
        return copyMenus(loadedMenus);
    }

    public List<Menu> getUserMenuTree(TenantId tenantId, UserId userId, Supplier<List<Menu>> loader) {
        String cacheKey = buildUserCacheKey(tenantId, userId);
        List<Menu> cachedMenus = userMenuTreeCache.get(cacheKey);
        if (cachedMenus != null) {
            return copyMenus(cachedMenus);
        }
        List<Menu> loadedMenus = copyMenus(loader.get());
        userMenuTreeCache.put(cacheKey, loadedMenus);
        return copyMenus(loadedMenus);
    }

    public Set<String> getUserPermissionCodes(TenantId tenantId, UserId userId, Supplier<Set<String>> loader) {
        String cacheKey = buildUserCacheKey(tenantId, userId);
        Set<String> cachedCodes = userPermissionCodeCache.get(cacheKey);
        if (cachedCodes != null) {
            return Set.copyOf(cachedCodes);
        }
        Set<String> loadedCodes = immutableStringSet(loader.get());
        userPermissionCodeCache.put(cacheKey, loadedCodes);
        return loadedCodes;
    }

    public Set<DepartmentId> getUserDepartmentIds(TenantId tenantId, UserId userId, Supplier<Set<DepartmentId>> loader) {
        String cacheKey = buildUserCacheKey(tenantId, userId);
        Set<DepartmentId> cachedDepartmentIds = userDepartmentIdsCache.get(cacheKey);
        if (cachedDepartmentIds != null) {
            return Set.copyOf(cachedDepartmentIds);
        }
        Set<DepartmentId> loadedDepartmentIds = immutableDepartmentIdSet(loader.get());
        userDepartmentIdsCache.put(cacheKey, loadedDepartmentIds);
        return loadedDepartmentIds;
    }

    public Set<String> getUserScopeTypes(TenantId tenantId, UserId userId, Supplier<Set<String>> loader) {
        String cacheKey = buildUserCacheKey(tenantId, userId);
        Set<String> cachedScopeTypes = userScopeTypesCache.get(cacheKey);
        if (cachedScopeTypes != null) {
            return Set.copyOf(cachedScopeTypes);
        }
        Set<String> loadedScopeTypes = immutableStringSet(loader.get());
        userScopeTypesCache.put(cacheKey, loadedScopeTypes);
        return loadedScopeTypes;
    }

    public void evictTenantPermission(TenantId tenantId) {
        tenantPermissionVersionCache.put(tenantId, System.nanoTime());
    }

    public void evictUserPermission(TenantId tenantId, UserId userId) {
        userPermissionVersionCache.put(buildUserVersionKey(tenantId, userId), System.nanoTime());
    }

    public void evictUsersPermission(TenantId tenantId, Collection<UserId> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        userIds.stream().distinct().forEach(userId -> evictUserPermission(tenantId, userId));
    }

    private String buildUserCacheKey(TenantId tenantId, UserId userId) {
        return tenantId + ":" + userId + ":" + getTenantPermissionVersion(tenantId) + ":" + getUserPermissionVersion(tenantId, userId);
    }

    private String buildUserVersionKey(TenantId tenantId, UserId userId) {
        return tenantId + ":" + userId;
    }

    private long getTenantPermissionVersion(TenantId tenantId) {
        Long version = tenantPermissionVersionCache.get(tenantId);
        return version == null ? INITIAL_VERSION : version;
    }

    private long getUserPermissionVersion(TenantId tenantId, UserId userId) {
        Long version = userPermissionVersionCache.get(buildUserVersionKey(tenantId, userId));
        return version == null ? INITIAL_VERSION : version;
    }

    private Set<DepartmentId> immutableDepartmentIdSet(Set<DepartmentId> values) {
        return values == null || values.isEmpty() ? Set.of() : Set.copyOf(values);
    }

    private Set<String> immutableStringSet(Set<String> values) {
        return values == null || values.isEmpty() ? Set.of() : Set.copyOf(values);
    }

    private List<Menu> copyMenus(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        return menus.stream().map(this::copyMenu).toList();
    }

    private Menu copyMenu(Menu menu) {
        return new Menu(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                copyMenus(menu.getChildren()));
    }
}
