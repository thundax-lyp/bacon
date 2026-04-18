package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.MenuPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleMenuRelDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import com.github.thundax.bacon.upms.infra.persistence.mapper.RoleMenuRelMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class MenuPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final MenuMapper menuMapper;
    private final RoleMenuRelMapper roleMenuRelMapper;

    MenuPersistenceSupport(MenuMapper menuMapper, RoleMenuRelMapper roleMenuRelMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuRelMapper = roleMenuRelMapper;
    }

    Optional<Menu> findById(MenuId menuId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(
                        menuMapper.selectOne(Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getId, menuId.value())))
                .map(MenuPersistenceAssembler::toDomain);
    }

    List<Menu> list() {
        BaconContextHolder.requireTenantId();
        return menuMapper.selectList(Wrappers.<MenuDO>lambdaQuery().orderByAsc(MenuDO::getSort, MenuDO::getId)).stream()
                .map(MenuPersistenceAssembler::toDomain)
                .toList();
    }

    boolean existsChild(MenuId menuId) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(menuMapper.selectCount(
                                Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getParentId, menuId.value())))
                        .orElse(0L)
                > 0L;
    }

    Menu insert(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(menu);
        menuMapper.insert(dataObject);
        return MenuPersistenceAssembler.toDomain(dataObject);
    }

    Menu update(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(menu);
        menuMapper.updateById(dataObject);
        return MenuPersistenceAssembler.toDomain(dataObject);
    }

    void delete(MenuId menuId) {
        BaconContextHolder.requireTenantId();
        roleMenuRelMapper.delete(Wrappers.<RoleMenuRelDO>lambdaQuery().eq(RoleMenuRelDO::getMenuId, menuId.value()));
        menuMapper.delete(Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getId, menuId.value()));
    }
}
