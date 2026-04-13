package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    List<Menu> listMenus();

    Optional<Menu> findMenuById(MenuId menuId);

    Menu save(Menu menu);

    Menu updateSort(MenuId menuId, Integer sort);

    void deleteMenu(MenuId menuId);

    boolean existsChildMenu(MenuId menuId);
}
