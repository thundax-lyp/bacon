package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuApplicationService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;

    public MenuApplicationService(MenuRepository menuRepository, PermissionRepository permissionRepository) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<UserMenuTreeDTO> toMenuTree(List<Menu> menus) {
        // 这里假设上游已经给出树形菜单，当前方法只做 DTO 投影，不再重复构树。
        return menus.stream().map(this::toDto).toList();
    }

    public List<MenuTreeDTO> getMenuTree(TenantId tenantId) {
        // 菜单树读取直接复用权限仓储结果，避免命令侧和权限侧各维护一套树装配逻辑。
        Long tenantIdValue = tenantId.value();
        return permissionRepository.listMenus(tenantId).stream().map(menu -> toTreeDto(menu, tenantIdValue)).toList();
    }

    @Transactional
    public MenuTreeDTO createMenu(TenantId tenantId, String menuType, String name, String parentId, String routePath,
                                  String componentName, String icon, Integer sort, String permissionCode) {
        validateRequired(menuType, "menuType");
        validateRequired(name, "name");
        MenuId domainParentId = normalizeParentId(parentId);
        validateParent(tenantId, domainParentId);
        return toTreeDto(menuRepository.save(new Menu(null, tenantId, normalize(menuType), normalize(name),
                domainParentId, normalize(routePath), normalize(componentName), normalize(icon),
                sort == null ? 0 : sort, normalize(permissionCode), List.of())));
    }

    @Transactional
    public MenuTreeDTO updateMenu(TenantId tenantId, String menuId, String menuType, String name, String parentId, String routePath,
                                  String componentName, String icon, Integer sort, String permissionCode) {
        MenuId domainMenuId = MenuId.of(Long.parseLong(menuId));
        MenuId domainParentId = normalizeParentId(parentId);
        Menu currentMenu = menuRepository.findMenuById(tenantId, domainMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        validateRequired(menuType, "menuType");
        validateRequired(name, "name");
        validateParent(tenantId, domainParentId);
        if (domainMenuId.equals(domainParentId)) {
            throw new IllegalArgumentException("Menu parent cannot be self");
        }
        return toTreeDto(menuRepository.save(new Menu(currentMenu.getId(), tenantId, normalize(menuType), normalize(name),
                domainParentId, normalize(routePath), normalize(componentName), normalize(icon),
                sort == null ? currentMenu.getSort() : sort, normalize(permissionCode), List.of())));
    }

    @Transactional
    public void deleteMenu(TenantId tenantId, String menuId) {
        MenuId domainMenuId = MenuId.of(Long.parseLong(menuId));
        menuRepository.findMenuById(tenantId, domainMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        if (menuRepository.existsChildMenu(tenantId, domainMenuId)) {
            throw new IllegalArgumentException("Menu has child menus: " + menuId);
        }
        menuRepository.deleteMenu(tenantId, domainMenuId);
    }

    @Transactional
    public MenuTreeDTO updateMenuSort(TenantId tenantId, String menuId, Integer sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
        return toTreeDto(menuRepository.updateSort(tenantId, MenuId.of(Long.parseLong(menuId)), sort));
    }

    private UserMenuTreeDTO toDto(Menu menu) {
        return new UserMenuTreeDTO(idValue(menu.getId()), menu.getName(), menu.getMenuType(), idValue(menu.getParentId()),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(),
                menu.getChildren() == null ? List.of() : menu.getChildren().stream().map(this::toDto).toList());
    }

    private MenuTreeDTO toTreeDto(Menu menu) {
        return toTreeDto(menu, menu.getTenantId().value());
    }

    private MenuTreeDTO toTreeDto(Menu menu, Long tenantIdValue) {
        return new MenuTreeDTO(idValue(menu.getId()), tenantIdValue, menu.getMenuType(),
                menu.getName(), idValue(menu.getParentId()),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                menu.getChildren() == null ? List.of() : menu.getChildren().stream()
                        .map(child -> toTreeDto(child, tenantIdValue))
                        .toList());
    }

    private void validateParent(TenantId tenantId, MenuId parentId) {
        // 菜单根节点统一用 null parentId 语义，避免和正数型 ID 约束冲突。
        if (parentId == null) {
            return;
        }
        menuRepository.findMenuById(tenantId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent menu not found: " + parentId));
    }

    private MenuId normalizeParentId(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return MenuId.of(Long.parseLong(parentId.trim()));
    }

    private Long idValue(MenuId menuId) {
        return menuId == null ? null : menuId.value();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
