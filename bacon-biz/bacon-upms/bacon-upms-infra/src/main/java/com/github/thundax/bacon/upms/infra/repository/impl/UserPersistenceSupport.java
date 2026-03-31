package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
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

    UserPersistenceSupport(UserMapper userMapper, UserIdentityMapper userIdentityMapper) {
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
    }

    Optional<User> findUserById(Long tenantId, Long userId) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getId, userId)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    Optional<User> findUserByAccount(Long tenantId, String account) {
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getAccount, account)
                        .eq(UserDO::getDeleted, false)))
                .map(this::toDomain);
    }

    Optional<UserIdentity> findUserIdentity(Long tenantId, String identityType, String identityValue) {
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getTenantId, tenantId)
                        .eq(UserIdentityDO::getIdentityType, identityType)
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getEnabled, true)))
                .map(this::toDomain);
    }

    List<User> listUsers(Long tenantId, String account, String name, String phone, String status, int pageNo, int pageSize) {
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

    long countUsers(Long tenantId, String account, String name, String phone, String status) {
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
        if (userDO.getId() == null) {
            userDO.setCreatedAt(now);
            userDO.setUpdatedAt(now);
            userMapper.insert(userDO);
        } else {
            userDO.setUpdatedAt(now);
            userMapper.updateById(userDO);
        }
        return toDomain(userDO);
    }

    void deleteUser(Long tenantId, Long userId) {
        userMapper.delete(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getTenantId, tenantId)
                .eq(UserDO::getId, userId));
    }

    void deleteUserIdentitiesByUser(Long tenantId, Long userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getTenantId, tenantId)
                .eq(UserIdentityDO::getUserId, userId));
    }

    void deleteUserIdentitiesByUserAndType(Long tenantId, Long userId, String identityType) {
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

    boolean hasActiveUserInDepartment(Long tenantId, Long departmentId) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getTenantId, tenantId)
                        .eq(UserDO::getDepartmentId, departmentId)
                        .eq(UserDO::getDeleted, false)))
                .orElse(0L) > 0L;
    }
}
