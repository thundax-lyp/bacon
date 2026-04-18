package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserRoleRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String USER_ROLE_REL_ID_BIZ_TAG = "upms_user_role_rel";

    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final UserCredentialMapper userCredentialMapper;
    private final UserRoleRelMapper userRoleRelMapper;
    private final IdGenerator idGenerator;

    UserPersistenceSupport(
            UserMapper userMapper,
            UserIdentityMapper userIdentityMapper,
            UserCredentialMapper userCredentialMapper,
            UserRoleRelMapper userRoleRelMapper,
            IdGenerator idGenerator) {
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.userCredentialMapper = userCredentialMapper;
        this.userRoleRelMapper = userRoleRelMapper;
        this.idGenerator = idGenerator;
    }

    Optional<User> findById(UserId userId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getId, userId.value())
                        .eq(UserDO::getDeleted, false)))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<User> findByAccount(String account) {
        return findIdentity(UserIdentityType.ACCOUNT, account)
                .flatMap(identity -> findById(identity.getUserId()));
    }

    Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<UserIdentity> findIdentity(UserId userId, UserIdentityType identityType) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getUserId, userId.value())
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<UserCredential> findCredential(UserId userId, UserCredentialType credentialType) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userCredentialMapper.selectOne(Wrappers.<UserCredentialDO>lambdaQuery()
                        .eq(UserCredentialDO::getUserId, userId.value())
                        .eq(
                                UserCredentialDO::getCredentialType,
                                credentialType == null ? null : credentialType.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    List<User> page(String account, String name, String phone, UserStatus status, int pageNo, int pageSize) {
        Set<Long> userIds = resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return List.of();
        }
        return userMapper
                .selectList(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getDeleted, false)
                        .in(userIds != null, UserDO::getId, userIds)
                        .like(hasText(name), UserDO::getName, name)
                        .eq(status != null, UserDO::getStatus, status.value())
                        .orderByAsc(UserDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(UserPersistenceAssembler::toDomain)
                .toList();
    }

    long count(String account, String name, String phone, UserStatus status) {
        Set<Long> userIds = resolveUserIdsByIdentityFilters(account, phone);
        if (userIds != null && userIds.isEmpty()) {
            return 0L;
        }
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getDeleted, false)
                        .in(userIds != null, UserDO::getId, userIds)
                        .like(hasText(name), UserDO::getName, name)
                        .eq(status != null, UserDO::getStatus, status.value())))
                .orElse(0L);
    }

    private Set<Long> resolveUserIdsByIdentityFilters(String account, String phone) {
        Set<Long> filteredUserIds = null;
        if (hasText(account)) {
            filteredUserIds = queryUserIdsByIdentityLike(UserIdentityType.ACCOUNT, account);
        }
        if (hasText(phone)) {
            Set<Long> phoneUserIds = queryUserIdsByIdentityLike(UserIdentityType.PHONE, phone);
            if (filteredUserIds == null) {
                filteredUserIds = phoneUserIds;
            } else {
                filteredUserIds.retainAll(phoneUserIds);
            }
        }
        return filteredUserIds;
    }

    private Set<Long> queryUserIdsByIdentityLike(UserIdentityType identityType, String identityValue) {
        return new LinkedHashSet<>(userIdentityMapper
                .selectList(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .like(UserIdentityDO::getIdentityValue, trim(identityValue))
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value()))
                .stream()
                .map(UserIdentityDO::getUserId)
                .toList());
    }

    User insertUser(User user) {
        UserDO userDO = UserPersistenceAssembler.toDataObject(user);
        userDO.setDeleted(false);
        userMapper.insert(userDO);
        return UserPersistenceAssembler.toDomain(userDO);
    }

    User updateUser(User user) {
        UserDO userDO = UserPersistenceAssembler.toDataObject(user);
        userMapper.updateById(userDO);
        return UserPersistenceAssembler.toDomain(userDO);
    }

    void delete(UserId userId) {
        BaconContextHolder.requireTenantId();
        UserDO userDO = userMapper.selectOne(
                Wrappers.<UserDO>lambdaQuery().eq(UserDO::getId, userId.value()).eq(UserDO::getDeleted, false));
        if (userDO == null) {
            return;
        }
        userDO.setDeleted(true);
        userMapper.updateById(userDO);
    }

    void replaceUserRoles(UserId userId, Collection<RoleId> roleIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        deleteUserRolesByUser(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (RoleId roleId : new LinkedHashSet<>(roleIds)) {
            userRoleRelMapper.insert(new UserRoleRelDO(
                    idGenerator.nextId(USER_ROLE_REL_ID_BIZ_TAG), tenantId.value(), userId.value(), roleId.value()));
        }
    }

    void deleteUserRolesByUser(UserId userId) {
        BaconContextHolder.requireTenantId();
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()));
    }

    void deleteUserIdentitiesByUser(UserId userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery().eq(UserIdentityDO::getUserId, userId.value()));
    }

    void deleteUserIdentitiesByUserAndType(UserId userId, UserIdentityType identityType) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getUserId, userId.value())
                .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value()));
    }

    UserIdentity saveIdentity(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserPersistenceAssembler.toDataObject(userIdentity);
        if (dataObject.getId() == null || userIdentityMapper.selectById(dataObject.getId()) == null) {
            userIdentityMapper.insert(dataObject);
        } else {
            userIdentityMapper.updateById(dataObject);
        }
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    UserCredential saveCredential(UserCredential userCredential) {
        UserCredentialDO dataObject = UserPersistenceAssembler.toDataObject(userCredential);
        if (dataObject.getId() == null || userCredentialMapper.selectById(dataObject.getId()) == null) {
            userCredentialMapper.insert(dataObject);
        } else {
            userCredentialMapper.updateById(dataObject);
        }
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    void deleteUserCredentialsByUser(UserId userId) {
        userCredentialMapper.delete(
                Wrappers.<UserCredentialDO>lambdaQuery().eq(UserCredentialDO::getUserId, userId.value()));
    }

    boolean hasActiveUserInDepartment(DepartmentId departmentId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                                .eq(UserDO::getDepartmentId, departmentId.value())
                                .eq(UserDO::getDeleted, false)))
                        .orElse(0L)
                > 0L;
    }
}
