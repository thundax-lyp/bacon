package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import java.util.List;

public final class MenuAssembler {

    private MenuAssembler() {}

    public static UserMenuTreeDTO toUserMenuTreeDto(Menu menu) {
        return new UserMenuTreeDTO(
                MenuIdCodec.toValue(menu.getId()),
                menu.getName(),
                menu.getMenuType(),
                MenuIdCodec.toValue(menu.getParentId()),
                menu.getRoutePath(),
                menu.getComponentName(),
                menu.getIcon(),
                menu.getSort(),
                menu.getChildren() == null
                        ? List.of()
                        : menu.getChildren().stream().map(MenuAssembler::toUserMenuTreeDto).toList());
    }

    public static MenuTreeDTO toTreeDto(Menu menu) {
        return new MenuTreeDTO(
                MenuIdCodec.toValue(menu.getId()),
                menu.getMenuType(),
                menu.getName(),
                MenuIdCodec.toValue(menu.getParentId()),
                menu.getRoutePath(),
                menu.getComponentName(),
                menu.getIcon(),
                menu.getSort(),
                menu.getPermissionCode(),
                menu.getChildren() == null
                        ? List.of()
                        : menu.getChildren().stream()
                                .map(MenuAssembler::toTreeDto)
                                .toList());
    }
}
