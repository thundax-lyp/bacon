package com.github.thundax.bacon.upms.interfaces.assembler;

import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.command.MenuCreateCommand;
import com.github.thundax.bacon.upms.application.command.MenuSortUpdateCommand;
import com.github.thundax.bacon.upms.application.command.MenuUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.application.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.interfaces.request.MenuCreateRequest;
import com.github.thundax.bacon.upms.interfaces.request.MenuSortUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.request.MenuUpdateRequest;
import com.github.thundax.bacon.upms.interfaces.response.MenuTreeResponse;
import com.github.thundax.bacon.upms.interfaces.response.UserMenuTreeResponse;
import java.util.List;

public final class MenuInterfaceAssembler {

    private MenuInterfaceAssembler() {}

    public static List<MenuTreeResponse> toTreeResponseList(List<MenuTreeDTO> dtos) {
        return dtos.stream().map(MenuTreeResponse::from).toList();
    }

    public static MenuCreateCommand toCreateCommand(MenuCreateRequest request) {
        return new MenuCreateCommand(
                MenuType.from(trimPreservingNull(request.menuType())),
                trimPreservingNull(request.name()),
                MenuIdCodec.toDomain(request.parentId()),
                trimPreservingNull(request.routePath()),
                trimPreservingNull(request.componentName()),
                trimPreservingNull(request.icon()),
                trimPreservingNull(request.permissionCode()));
    }

    public static MenuUpdateCommand toUpdateCommand(Long menuId, MenuUpdateRequest request) {
        return new MenuUpdateCommand(
                MenuIdCodec.toDomain(menuId),
                MenuType.from(trimPreservingNull(request.menuType())),
                trimPreservingNull(request.name()),
                MenuIdCodec.toDomain(request.parentId()),
                trimPreservingNull(request.routePath()),
                trimPreservingNull(request.componentName()),
                trimPreservingNull(request.icon()),
                request.sort(),
                trimPreservingNull(request.permissionCode()));
    }

    public static MenuSortUpdateCommand toSortUpdateCommand(Long menuId, MenuSortUpdateRequest request) {
        return new MenuSortUpdateCommand(MenuIdCodec.toDomain(menuId), request.sort());
    }

    public static MenuId toMenuId(Long menuId) {
        return MenuIdCodec.toDomain(menuId);
    }

    public static MenuTreeResponse toResponse(MenuTreeDTO dto) {
        return MenuTreeResponse.from(dto);
    }

    public static List<UserMenuTreeResponse> toUserMenuTreeResponseList(List<UserMenuTreeDTO> dtos) {
        return dtos.stream().map(UserMenuTreeResponse::from).toList();
    }

    private static String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
