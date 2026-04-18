package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.RolePersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserRoleRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class RolePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final RoleMapper roleMapper;
    private final UserRoleRelMapper userRoleRelMapper;

    RolePersistenceSupport(RoleMapper roleMapper, UserRoleRelMapper userRoleRelMapper) {
        this.roleMapper = roleMapper;
        this.userRoleRelMapper = userRoleRelMapper;
    }

    Optional<Role> findById(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(
                        roleMapper.selectOne(Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getId, roleId.value())))
                .map(RolePersistenceAssembler::toDomain);
    }

    List<Role> findByUserId(UserId userId) {
        BaconContextHolder.requireTenantId();
        List<Long> roleIds =
                userRoleRelMapper
                        .selectList(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()))
                        .stream()
                        .map(UserRoleRelDO::getRoleId)
                        .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .in(RoleDO::getId, roleIds)
                        .orderByAsc(RoleDO::getId))
                .stream()
                .map(RolePersistenceAssembler::toDomain)
                .toList();
    }

    List<UserId> findAssignedUserIds(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        return userRoleRelMapper
                .selectList(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getRoleId, roleId.value()))
                .stream()
                .map(UserRoleRelDO::getUserId)
                .map(UserId::of)
                .distinct()
                .toList();
    }

    List<Role> page(RoleCode code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize) {
        return roleMapper
                .selectList(Wrappers.<RoleDO>lambdaQuery()
                        .like(code != null, RoleDO::getCode, code == null ? null : code.value())
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(roleType != null, RoleDO::getRoleType, roleType.value())
                        .eq(status != null, RoleDO::getStatus, status.value())
                        .orderByAsc(RoleDO::getId)
                        .last(limit(pageNo, pageSize)))
                .stream()
                .map(RolePersistenceAssembler::toDomain)
                .toList();
    }

    long count(RoleCode code, String name, RoleType roleType, RoleStatus status) {
        return Optional.ofNullable(roleMapper.selectCount(Wrappers.<RoleDO>lambdaQuery()
                        .like(code != null, RoleDO::getCode, code == null ? null : code.value())
                        .like(hasText(name), RoleDO::getName, name)
                        .eq(roleType != null, RoleDO::getRoleType, roleType.value())
                        .eq(status != null, RoleDO::getStatus, status.value())))
                .orElse(0L);
    }

    Role insert(Role role) {
        RoleDO roleDO = RolePersistenceAssembler.toDataObject(role);
        roleMapper.insert(roleDO);
        return RolePersistenceAssembler.toDomain(roleDO);
    }

    Role update(Role role) {
        RoleDO roleDO = RolePersistenceAssembler.toDataObject(role);
        roleMapper.updateById(roleDO);
        return RolePersistenceAssembler.toDomain(roleDO);
    }

    void delete(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        roleMapper.delete(Wrappers.<RoleDO>lambdaQuery().eq(RoleDO::getId, roleId.value()));
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getRoleId, roleId.value()));
    }
}
