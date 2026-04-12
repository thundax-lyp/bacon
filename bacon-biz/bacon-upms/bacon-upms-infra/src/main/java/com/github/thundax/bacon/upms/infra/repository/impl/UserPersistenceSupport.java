package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final UserCredentialMapper userCredentialMapper;

    UserPersistenceSupport(
            UserMapper userMapper, UserIdentityMapper userIdentityMapper, UserCredentialMapper userCredentialMapper) {
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.userCredentialMapper = userCredentialMapper;
    }

    Optional<User> findUserById(TenantId tenantId, UserId userId) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getId, userId)
                        .eq(UserDO::getDeleted, false)))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<User> findUserByAccount(TenantId tenantId, String account) {
        return findUserIdentity(tenantId, UserIdentityType.ACCOUNT, account)
                .flatMap(identity -> findUserById(tenantId, identity.getUserId()));
    }

    Optional<UserIdentity> findUserIdentity(TenantId tenantId, UserIdentityType identityType, String identityValue) {
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getTenantId, tenantId)
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<UserIdentity> findUserIdentityByUserId(TenantId tenantId, UserId userId, UserIdentityType identityType) {
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getTenantId, tenantId)
                        .eq(UserIdentityDO::getUserId, userId)
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<UserCredential> findUserCredential(TenantId tenantId, UserId userId, UserCredentialType credentialType) {
        return Optional.ofNullable(userCredentialMapper.selectOne(Wrappers.<UserCredentialDO>lambdaQuery()
                        .eq(UserCredentialDO::getTenantId, tenantId)
                        .eq(UserCredentialDO::getUserId, userId)
                        .eq(
                                UserCredentialDO::getCredentialType,
                                credentialType == null ? null : credentialType.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    List<User> listUsers(String account, String name, String phone, String status, int pageNo, int pageSize) {
        Set<UserId> userIds = resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return List.of();
        }
        return userMapper
                .selectList(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getDeleted, false)
                        .in(userIds != null, UserDO::getId, userIds)
                        .like(hasText(name), UserDO::getName, name)
                        .eq(hasText(status), UserDO::getStatus, trim(status))
                        .orderByAsc(UserDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(UserPersistenceAssembler::toDomain)
                .toList();
    }

    long countUsers(String account, String name, String phone, String status) {
        Set<UserId> userIds = resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return 0L;
        }
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getDeleted, false)
                        .in(userIds != null, UserDO::getId, userIds)
                        .like(hasText(name), UserDO::getName, name)
                        .eq(hasText(status), UserDO::getStatus, trim(status))))
                .orElse(0L);
    }

    private Set<UserId> resolveUserIdsByIdentityFilters(String account, String phone) {
        Set<UserId> filteredUserIds = null;
        if (hasText(account)) {
            filteredUserIds = queryUserIdsByIdentityLike(UserIdentityType.ACCOUNT, account);
        }
        if (hasText(phone)) {
            Set<UserId> phoneUserIds = queryUserIdsByIdentityLike(UserIdentityType.PHONE, phone);
            if (filteredUserIds == null) {
                filteredUserIds = phoneUserIds;
            } else {
                filteredUserIds.retainAll(phoneUserIds);
            }
        }
        return filteredUserIds;
    }

    private Set<UserId> queryUserIdsByIdentityLike(UserIdentityType identityType, String identityValue) {
        return new LinkedHashSet<>(userIdentityMapper
                .selectList(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .like(UserIdentityDO::getIdentityValue, trim(identityValue))
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value()))
                .stream()
                .map(UserIdentityDO::getUserId)
                .toList());
    }

    User saveUser(User user) {
        UserDO userDO = UserPersistenceAssembler.toDataObject(user);
        boolean insert = userDO.getId() == null || userMapper.selectById(userDO.getId()) == null;
        if (insert) {
            userDO.setDeleted(false);
            userMapper.insert(userDO);
        } else {
            userMapper.updateById(userDO);
        }
        return UserPersistenceAssembler.toDomain(userDO);
    }

    void deleteUser(TenantId tenantId, UserId userId) {
        UserDO userDO = userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getTenantId, tenantId)
                .eq(UserDO::getId, userId)
                .eq(UserDO::getDeleted, false));
        if (userDO == null) {
            return;
        }
        userDO.setDeleted(true);
        userMapper.updateById(userDO);
    }

    void deleteUserIdentitiesByUser(TenantId tenantId, UserId userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId));
    }

    void deleteUserIdentitiesByUserAndType(TenantId tenantId, UserId userId, UserIdentityType identityType) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId)
                .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value()));
    }

    UserIdentity saveUserIdentity(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserPersistenceAssembler.toDataObject(userIdentity);
        if (dataObject.getId() == null || userIdentityMapper.selectById(dataObject.getId()) == null) {
            userIdentityMapper.insert(dataObject);
        } else {
            userIdentityMapper.updateById(dataObject);
        }
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    UserCredential saveUserCredential(UserCredential userCredential) {
        UserCredentialDO dataObject = UserPersistenceAssembler.toDataObject(userCredential);
        if (dataObject.getId() == null || userCredentialMapper.selectById(dataObject.getId()) == null) {
            userCredentialMapper.insert(dataObject);
        } else {
            userCredentialMapper.updateById(dataObject);
        }
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    void deleteUserCredentialsByUser(TenantId tenantId, UserId userId) {
        userCredentialMapper.delete(Wrappers.<UserCredentialDO>lambdaQuery()
                .eq(UserCredentialDO::getTenantId, tenantId)
                .eq(UserCredentialDO::getUserId, userId));
    }

    boolean hasActiveUserInDepartment(TenantId tenantId, DepartmentId departmentId) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                                .eq(UserDO::getTenantId, tenantId)
                                .eq(UserDO::getDepartmentId, departmentId)
                                .eq(UserDO::getDeleted, false)))
                        .orElse(0L)
                > 0L;
    }
}
