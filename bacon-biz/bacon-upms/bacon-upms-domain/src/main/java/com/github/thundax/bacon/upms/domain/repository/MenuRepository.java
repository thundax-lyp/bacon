package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {

    List<Menu> list();

    Optional<Menu> findById(MenuId menuId);

    Menu insert(Menu menu);

    Menu update(Menu menu);

    void delete(MenuId menuId);

    boolean existsChild(MenuId menuId);
}
