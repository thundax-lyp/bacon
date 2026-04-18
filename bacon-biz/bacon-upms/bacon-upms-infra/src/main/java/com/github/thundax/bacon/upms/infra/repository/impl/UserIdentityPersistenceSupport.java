package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.infra.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserIdentityMapper;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserIdentityPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final UserIdentityMapper userIdentityMapper;

    UserIdentityPersistenceSupport(UserIdentityMapper userIdentityMapper) {
        this.userIdentityMapper = userIdentityMapper;
    }

    Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getIdentityValue, identityValue)
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Optional<UserIdentity> findIdentityByUserId(UserId userId, UserIdentityType identityType) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userIdentityMapper.selectOne(Wrappers.<UserIdentityDO>lambdaQuery()
                        .eq(UserIdentityDO::getUserId, userId.value())
                        .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value())
                        .eq(UserIdentityDO::getStatus, UserIdentityStatus.ACTIVE.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    Set<Long> resolveUserIdsByIdentityFilters(String account, String phone) {
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

    void deleteIdentitiesByUserId(UserId userId) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery().eq(UserIdentityDO::getUserId, userId.value()));
    }

    void deleteIdentityByUserIdAndType(UserId userId, UserIdentityType identityType) {
        userIdentityMapper.delete(Wrappers.<UserIdentityDO>lambdaQuery()
                .eq(UserIdentityDO::getUserId, userId.value())
                .eq(UserIdentityDO::getIdentityType, identityType == null ? null : identityType.value()));
    }

    UserIdentity insert(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserPersistenceAssembler.toDataObject(userIdentity);
        userIdentityMapper.insert(dataObject);
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    UserIdentity update(UserIdentity userIdentity) {
        UserIdentityDO dataObject = UserPersistenceAssembler.toDataObject(userIdentity);
        userIdentityMapper.updateById(dataObject);
        return UserPersistenceAssembler.toDomain(dataObject);
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
}
