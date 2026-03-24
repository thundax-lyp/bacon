package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.entity.Tenant;
import com.github.thundax.bacon.upms.domain.entity.User;
import com.github.thundax.bacon.upms.domain.entity.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findUserById(Long tenantId, Long userId);

    Optional<User> findUserByAccount(Long tenantId, String account);

    Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue);

    Optional<Tenant> findTenantByTenantId(Long tenantId);

    Optional<Tenant> findTenantByCode(String code);

    List<Tenant> pageTenants(Long tenantId, String code, String name, String status, int pageNo, int pageSize);

    long countTenants(Long tenantId, String code, String name, String status);

    Tenant saveTenant(Tenant tenant);

    Tenant updateTenantStatus(Long tenantId, String status);

    List<User> pageUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize);

    long countUsers(Long tenantId, String account, String name, String phone, String status);

    List<User> listUsers(Long tenantId, String account, String name, String phone, String status);

    User save(User user);

    User updatePassword(Long tenantId, Long userId, String passwordHash);

    List<Role> assignRoles(Long tenantId, Long userId, List<Long> roleIds);

    void deleteUser(Long tenantId, Long userId);
}
