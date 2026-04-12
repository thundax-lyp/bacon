package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import java.util.List;

public final class MenuPersistenceAssembler {

    private MenuPersistenceAssembler() {}

    public static MenuDO toDataObject(Menu menu) {
        return new MenuDO(
                menu.getId(),
                menu.getTenantId(),
                menu.getMenuType(),
                menu.getName(),
                menu.getParentId(),
                menu.getRoutePath(),
                menu.getComponentName(),
                menu.getIcon(),
                menu.getSort(),
                menu.getPermissionCode());
    }

    public static Menu toDomain(MenuDO dataObject) {
        MenuId parentId = dataObject.getParentId();
        return Menu.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getMenuType(),
                dataObject.getName(),
                parentId,
                dataObject.getRoutePath(),
                dataObject.getComponentName(),
                dataObject.getIcon(),
                dataObject.getSort(),
                dataObject.getPermissionCode(),
                List.of());
    }
}
