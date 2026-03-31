package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findUserById(Long tenantId, UserId userId);

    Optional<User> findUserByAccount(Long tenantId, String account);

    Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue);

    Optional<UserCredential> findUserCredential(Long tenantId, UserId userId, String credentialType);

    List<User> pageUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize);

    long countUsers(Long tenantId, String account, String name, String phone, String status);

    List<User> listUsers(Long tenantId, String account, String name, String phone, String status);

    User save(User user);

    User updatePassword(Long tenantId, UserId userId, String passwordHash, boolean needChangePassword);

    List<Role> assignRoles(Long tenantId, UserId userId, List<Long> roleIds);

    void deleteUser(Long tenantId, UserId userId);
}
