package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final UserMapper userMapper;

    UserPersistenceSupport(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    Optional<User> findById(UserId userId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getId, userId.value())
                        .eq(UserDO::getDeleted, false)))
                .map(UserPersistenceAssembler::toDomain);
    }

    List<User> page(Set<Long> userIds, String name, UserStatus status, int pageNo, int pageSize) {
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

    long count(Set<Long> userIds, String name, UserStatus status) {
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getDeleted, false)
                        .in(userIds != null, UserDO::getId, userIds)
                        .like(hasText(name), UserDO::getName, name)
                        .eq(status != null, UserDO::getStatus, status.value())))
                .orElse(0L);
    }

    boolean existsActiveByDepartmentId(DepartmentId departmentId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userMapper.selectCount(Wrappers.<UserDO>lambdaQuery()
                                .eq(UserDO::getDepartmentId, departmentId.value())
                                .eq(UserDO::getDeleted, false)))
                        .orElse(0L)
                > 0L;
    }

    User insert(User user) {
        UserDO userDO = UserPersistenceAssembler.toDataObject(user);
        userDO.setDeleted(false);
        userMapper.insert(userDO);
        return UserPersistenceAssembler.toDomain(userDO);
    }

    User update(User user) {
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
}
