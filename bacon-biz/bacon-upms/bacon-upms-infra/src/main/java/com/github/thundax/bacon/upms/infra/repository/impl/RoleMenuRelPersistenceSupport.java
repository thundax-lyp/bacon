package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleMenuRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class RoleMenuRelPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private static final String ROLE_MENU_REL_ID_BIZ_TAG = "upms-role-menu-rel-id";

    private final RoleMenuRelMapper roleMenuRelMapper;
    private final IdGenerator idGenerator;

    RoleMenuRelPersistenceSupport(RoleMenuRelMapper roleMenuRelMapper, IdGenerator idGenerator) {
        this.roleMenuRelMapper = roleMenuRelMapper;
        this.idGenerator = idGenerator;
    }

    Set<MenuId> findMenuIds(RoleId roleId) {
        BaconContextHolder.requireTenantId();
        return roleMenuRelMapper
                .selectList(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getRoleId, roleId.value()))
                .stream()
                .map(RoleMenuRelDO::getMenuId)
                .map(MenuId::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    void updateMenuIds(RoleId roleId, Collection<MenuId> menuIds) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getRoleId, roleId.value()));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (MenuId menuId : new LinkedHashSet<>(menuIds)) {
            roleMenuRelMapper.insert(new RoleMenuRelDO(
                    idGenerator.nextId(ROLE_MENU_REL_ID_BIZ_TAG), tenantId.value(), roleId.value(), menuId.value()));
        }
    }

    void removeMenuFromAssignments(MenuId menuId) {
        BaconContextHolder.requireTenantId();
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getMenuId, menuId.value()));
    }
}
