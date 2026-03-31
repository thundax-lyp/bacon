package com.github.thundax.bacon.upms.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import com.github.thundax.bacon.upms.infra.persistence.mapper.MenuMapper;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
class MenuPersistenceSupport extends AbstractUpmsPersistenceSupport {

    private final MenuMapper menuMapper;

    MenuPersistenceSupport(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    List<Menu> listMenus(TenantId tenantId) {
        return menuMapper.selectList(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .orderByAsc(MenuDO::getSort, MenuDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    Optional<Menu> findMenuById(TenantId tenantId, Long menuId) {
        return Optional.ofNullable(menuMapper.selectOne(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .eq(MenuDO::getId, menuId)))
                .map(this::toDomain);
    }

    Menu saveMenu(Menu menu) {
        MenuDO dataObject = toDataObject(menu);
        if (dataObject.getId() == null) {
            menuMapper.insert(dataObject);
        } else {
            menuMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    void deleteMenu(TenantId tenantId, Long menuId) {
        menuMapper.delete(Wrappers.<MenuDO>lambdaQuery()
                .eq(MenuDO::getTenantId, tenantId)
                .eq(MenuDO::getId, menuId));
    }

    boolean existsChildMenu(TenantId tenantId, Long menuId) {
        return Optional.ofNullable(menuMapper.selectCount(Wrappers.<MenuDO>lambdaQuery()
                        .eq(MenuDO::getTenantId, tenantId)
                        .eq(MenuDO::getParentId, menuId)))
                .orElse(0L) > 0L;
    }
}
