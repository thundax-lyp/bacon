package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
class UserPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final UserCredentialMapper userCredentialMapper;

    UserPersistenceSupport(UserMapper userMapper, UserIdentityMapper userIdentityMapper,
                           UserCredentialMapper userCredentialMapper) {
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.userCredentialMapper = userCredentialMapper;
    }

    Optional<User> findUserById(TenantId tenantId, UserId userId) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getId, userId)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    Optional<User> findUserByAccount(TenantId tenantId, String account) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getAccount, account)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    Optional<UserIdentity> findUserIdentity(TenantId tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getTenantId, tenantId)
                        .eq(UserIdentityDO::getIdentityType, identityType)
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getEnabled, true)))
                .map(this::toDomain);
    }

    Optional<UserCredential> findUserCredential(TenantId tenantId, UserId userId, String credentialType) {
        return Optional.ofNullable(userCredentialMapper.selectOne(Wrappers.<UserCredentialDO>lambdaQuery()
                        .eq(UserCredentialDO::getTenantId, tenantId)
                        .eq(UserCredentialDO::getUserId, userId)
                        .eq(UserCredentialDO::getCredentialType, credentialType)))
                .map(this::toDomain);
    }

    List<User> listUsers(TenantId tenantId, String account, String name, String phone, String status, int pageNo,
                         int pageSize) {
        return userMapper.selectList(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDeleted, false)
                        .like(hasText(account), UserDO::getAccount, account)
                        .like(hasText(name), UserDO::getName, name)
                        .like(hasText(phone), UserDO::getPhone, phone)
                        .eq(hasText(status), UserDO::getStatus, trim(status))
                        .orderByAsc(UserDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    long countUsers(TenantId tenantId, String account, String name, String phone, String status) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDeleted, false)
                        .like(hasText(account), UserDO::getAccount, account)
                        .like(hasText(name), UserDO::getName, name)
                        .like(hasText(phone), UserDO::getPhone, phone)
                        .eq(hasText(status), UserDO::getStatus, trim(status))))
                .orElse(0L);
    }

    User saveUser(User user) {
        UserDO userDO = toDataObject(user);
        LocalDateTime now = LocalDateTime.now();
        boolean insert = userDO.getId() == null || userMapper.selectById(userDO.getId()) == null;
        if (insert) {
            userDO.setDeleted(false);
            userDO.setCreatedAt(now);
            userDO.setUpdatedAt(now);
            userMapper.insert(userDO);
        } else {
            userDO.setUpdatedAt(now);
            userMapper.updateById(userDO);
        }
        return toDomain(userDO);
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
        userDO.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(userDO);
    }

    void deleteUserIdentitiesByUser(TenantId tenantId, UserId userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId));
    }

    void deleteUserIdentitiesByUserAndType(TenantId tenantId, UserId userId, String identityType) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId)
                .eq(UserIdentityDO::getIdentityType, identityType));
    }

    UserIdentity saveUserIdentity(UserIdentity userIdentity) {
        UserIdentityDO dataObject = toDataObject(userIdentity);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            userIdentityMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            userIdentityMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    UserCredential saveUserCredential(UserCredential userCredential) {
        UserCredentialDO dataObject = toDataObject(userCredential);
        LocalDateTime now = LocalDateTime.now();
        if (dataObject.getId() == null) {
            dataObject.setCreatedAt(now);
            dataObject.setUpdatedAt(now);
            userCredentialMapper.insert(dataObject);
        } else {
            dataObject.setUpdatedAt(now);
            userCredentialMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    void deleteUserCredentialsByUser(TenantId tenantId, UserId userId) {
        userCredentialMapper.delete(Wrappers.<UserCredentialDO>lambdaQuery()
                .eq(UserCredentialDO::getTenantId, tenantId)
                .eq(UserCredentialDO::getUserId, userId));
    }

    boolean hasActiveUserInDepartment(TenantId tenantId, Long departmentId) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDepartmentId, departmentId)
                        .eq(UserDO::getDeleted, false)))
                .orElse(0L) > 0L;
    }
}
