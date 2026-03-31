package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    List<Menu> listMenus(TenantId tenantId);

    Optional<Menu> findMenuById(TenantId tenantId, Long menuId);

    Menu save(Menu menu);

    Menu updateSort(TenantId tenantId, Long menuId, Integer sort);

    void deleteMenu(TenantId tenantId, Long menuId);

    boolean existsChildMenu(TenantId tenantId, Long menuId);
}
