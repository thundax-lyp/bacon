package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.infra.persistence.assembler.UserPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserCredentialMapper;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserCredentialPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final UserCredentialMapper userCredentialMapper;

    UserCredentialPersistenceSupport(UserCredentialMapper userCredentialMapper) {
        this.userCredentialMapper = userCredentialMapper;
    }

    Optional<UserCredential> findCredentialByUserId(UserId userId, UserCredentialType credentialType) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(userCredentialMapper.selectOne(Wrappers.<UserCredentialDO>lambdaQuery()
                        .eq(UserCredentialDO::getUserId, userId.value())
                        .eq(
                                UserCredentialDO::getCredentialType,
                                credentialType == null ? null : credentialType.value())))
                .map(UserPersistenceAssembler::toDomain);
    }

    UserCredential insert(UserCredential userCredential) {
        UserCredentialDO dataObject = UserPersistenceAssembler.toDataObject(userCredential);
        userCredentialMapper.insert(dataObject);
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    UserCredential update(UserCredential userCredential) {
        UserCredentialDO dataObject = UserPersistenceAssembler.toDataObject(userCredential);
        userCredentialMapper.updateById(dataObject);
        return UserPersistenceAssembler.toDomain(dataObject);
    }

    void deleteCredentialsByUserId(UserId userId) {
        userCredentialMapper.delete(
                Wrappers.<UserCredentialDO>lambdaQuery().eq(UserCredentialDO::getUserId, userId.value()));
    }
}
