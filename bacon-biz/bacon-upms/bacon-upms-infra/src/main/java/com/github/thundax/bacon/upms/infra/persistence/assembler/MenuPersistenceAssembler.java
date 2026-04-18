package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.MenuDO;
import java.util.List;

public final class MenuPersistenceAssembler {

    private MenuPersistenceAssembler() {}

    public static MenuDO toDataObject(Menu menu) {
        return new MenuDO(
                menu.getId() == null ? null : menu.getId().value(),
                BaconContextHolder.requireTenantId(),
                menu.getMenuType() == null ? null : menu.getMenuType().value(),
                menu.getName(),
                menu.getParentId() == null ? null : menu.getParentId().value(),
                menu.getRoutePath(),
                menu.getComponentName(),
                menu.getIcon(),
                menu.getSort(),
                menu.getPermissionCode());
    }

    public static Menu toDomain(MenuDO dataObject) {
        MenuId parentId = dataObject.getParentId() == null ? null : MenuId.of(dataObject.getParentId());
        return Menu.reconstruct(
                dataObject.getId() == null ? null : MenuId.of(dataObject.getId()),
                dataObject.getMenuType() == null ? null : MenuType.from(dataObject.getMenuType()),
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
