package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    List<Menu> listMenus(Long tenantId);

    Optional<Menu> findMenuById(Long tenantId, Long menuId);

    Menu save(Menu menu);

    Menu updateSort(Long tenantId, Long menuId, Integer sort);

    void deleteMenu(Long tenantId, Long menuId);

    boolean existsChildMenu(Long tenantId, Long menuId);
}
