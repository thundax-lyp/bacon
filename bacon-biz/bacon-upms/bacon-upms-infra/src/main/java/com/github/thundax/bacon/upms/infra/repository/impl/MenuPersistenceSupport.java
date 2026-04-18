package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.infra.persistence.assembler.MenuPersistenceAssembler;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
class MenuPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final MenuMapper menuMapper;

    MenuPersistenceSupport(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    List<Menu> listMenus() {
        requireTenantId();
        return menuMapper.selectList(Wrappers.<MenuDO>lambdaQuery().orderByAsc(MenuDO::getSort, MenuDO::getId)).stream()
                .map(MenuPersistenceAssembler::toDomain)
                .toList();
    }

    Optional<Menu> findById(MenuId menuId) {
        requireTenantId();
        return Optional.ofNullable(
                        menuMapper.selectOne(Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getId, menuId.value())))
                .map(MenuPersistenceAssembler::toDomain);
    }

    Menu insertMenu(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(menu);
        menuMapper.insert(dataObject);
        return MenuPersistenceAssembler.toDomain(dataObject);
    }

    Menu updateMenu(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(menu);
        menuMapper.updateById(dataObject);
        return MenuPersistenceAssembler.toDomain(dataObject);
    }

    void delete(MenuId menuId) {
        requireTenantId();
        menuMapper.delete(Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getId, menuId.value()));
    }

    boolean existsChildMenu(MenuId menuId) {
        requireTenantId();
        return Optional.ofNullable(menuMapper.selectCount(
                                Wrappers.<MenuDO>lambdaQuery().eq(MenuDO::getParentId, menuId.value())))
                        .orElse(0L)
                > 0L;
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
