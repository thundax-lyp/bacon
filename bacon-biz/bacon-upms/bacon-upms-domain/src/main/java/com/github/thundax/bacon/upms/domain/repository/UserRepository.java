package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId userId);

    Optional<User> findByAccount(String account);

    Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue);

    Optional<UserIdentity> findIdentityByUserId(UserId userId, UserIdentityType identityType);

    Optional<UserCredential> findCredentialByUserId(UserId userId, UserCredentialType credentialType);

    List<User> page(String account, String name, String phone, UserStatus status, int pageNo, int pageSize);

    long count(String account, String name, String phone, UserStatus status);

    List<User> listUsers(String account, String name, String phone, UserStatus status);

    User insert(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialIdIfAbsent);

    User update(
            User user,
            String account,
            String phone,
            UserIdentityId accountIdentityId,
            UserIdentityId phoneIdentityId,
            UserCredentialId passwordCredentialIdIfAbsent);

    User updatePassword(
            UserId userId, String password, boolean needChangePassword, UserCredentialId passwordCredentialIdIfAbsent);

    List<Role> updateRoleIds(UserId userId, List<RoleId> roleIds);

    void delete(UserId userId);
}
