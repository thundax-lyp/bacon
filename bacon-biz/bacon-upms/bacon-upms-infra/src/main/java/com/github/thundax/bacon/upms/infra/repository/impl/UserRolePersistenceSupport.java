package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserRoleRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.UserRoleRelMapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class UserRolePersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String USER_ROLE_REL_ID_BIZ_TAG = "upms_user_role_rel";

    private final UserRoleRelMapper userRoleRelMapper;
    private final IdGenerator idGenerator;

    UserRolePersistenceSupport(UserRoleRelMapper userRoleRelMapper, IdGenerator idGenerator) {
        this.userRoleRelMapper = userRoleRelMapper;
        this.idGenerator = idGenerator;
    }

    void updateRoleIds(UserId userId, Collection<RoleId> roleIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        deleteRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (RoleId roleId : new LinkedHashSet<>(roleIds)) {
            userRoleRelMapper.insert(new UserRoleRelDO(
                    idGenerator.nextId(USER_ROLE_REL_ID_BIZ_TAG), tenantId.value(), userId.value(), roleId.value()));
        }
    }

    void deleteRoleIdsByUserId(UserId userId) {
        BaconIdContextHelper.requireTenantId();
        userRoleRelMapper.delete(Wrappers.<UserRoleRelDO>lambdaQuery().eq(UserRoleRelDO::getUserId, userId.value()));
    }
}
