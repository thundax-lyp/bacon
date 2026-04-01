package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findUserById(TenantId tenantId, UserId userId);

    Optional<User> findUserByAccount(TenantId tenantId, String account);

    Optional<UserIdentity> findUserIdentity(TenantId tenantId, String identityType, String identityValue);

    Optional<UserCredential> findUserCredential(TenantId tenantId, UserId userId, String credentialType);

    List<User> pageUsers(TenantId tenantId, String account, String name, String phone, String status, int pageNo, int pageSize);

    long countUsers(TenantId tenantId, String account, String name, String phone, String status);

    List<User> listUsers(TenantId tenantId, String account, String name, String phone, String status);

    User save(User user);

    User updatePassword(TenantId tenantId, UserId userId, String passwordHash, boolean needChangePassword);

    List<Role> assignRoles(TenantId tenantId, UserId userId, List<RoleId> roleIds);

    void deleteUser(TenantId tenantId, UserId userId);
}
