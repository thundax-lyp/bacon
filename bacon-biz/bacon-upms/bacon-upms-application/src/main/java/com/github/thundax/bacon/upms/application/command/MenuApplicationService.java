package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.application.assembler.MenuAssembler;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuApplicationService {

    private static final String MENU_ID_BIZ_TAG = "menu-id";

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final IdGenerator idGenerator;

    public MenuApplicationService(
            MenuRepository menuRepository, PermissionRepository permissionRepository, IdGenerator idGenerator) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
        this.idGenerator = idGenerator;
    }

    public List<UserMenuTreeDTO> toMenuTree(List<Menu> menus) {
        // 这里假设上游已经给出树形菜单，当前方法只做 DTO 投影，不再重复构树。
        return menus.stream().map(MenuAssembler::toUserMenuTreeDto).toList();
    }

    public List<MenuTreeDTO> getMenuTree() {
        // 菜单树读取直接复用权限仓储结果，避免命令侧和权限侧各维护一套树装配逻辑。
        return permissionRepository.listMenus().stream()
                .map(MenuAssembler::toTreeDto)
                .toList();
    }

    @Transactional
    public MenuTreeDTO createMenu(
            MenuType menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            String permissionCode) {
        validateParent(parentId);
        return toTreeDto(menuRepository.insert(Menu.create(
                MenuIdCodec.toDomain(idGenerator.nextId(MENU_ID_BIZ_TAG)),
                menuType.value(),
                name,
                parentId,
                routePath,
                componentName,
                icon,
                permissionCode)));
    }

    @Transactional
    public MenuTreeDTO updateMenu(
            MenuId menuId,
            MenuType menuType,
            String name,
            MenuId parentId,
            String routePath,
            String componentName,
            String icon,
            Integer sort,
            String permissionCode) {
        Menu currentMenu = menuRepository
                .findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu not found: " + menuId));
        validateParent(parentId);
        currentMenu.retypeAs(menuType.value());
        currentMenu.rename(name);
        currentMenu.moveUnder(parentId);
        currentMenu.routeTo(routePath);
        currentMenu.renderWith(componentName);
        currentMenu.showIcon(icon);
        currentMenu.bindPermission(permissionCode);
        if (sort != null) {
            currentMenu.sort(sort);
        }
        return toTreeDto(menuRepository.update(currentMenu));
    }

    @Transactional
    public void delete(MenuId menuId) {
        menuRepository
                .findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu not found: " + menuId));
        if (menuRepository.existsChildMenu(menuId)) {
            throw new ConflictException("Menu has child menus: " + menuId);
        }
        menuRepository.delete(menuId);
    }

    @Transactional
    public MenuTreeDTO updateMenuSort(MenuId menuId, Integer sort) {
        if (sort == null) {
            throw new BadRequestException("sort must not be null");
        }
        return toTreeDto(menuRepository.updateSort(menuId, sort));
    }

    private MenuTreeDTO toTreeDto(Menu menu) {
        return MenuAssembler.toTreeDto(menu);
    }

    private void validateParent(MenuId parentId) {
        // 菜单根节点统一用 null parentId 语义，避免和正数型 ID 约束冲突。
        if (parentId == null) {
            return;
        }
        menuRepository
                .findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent menu not found: " + parentId));
    }
}
