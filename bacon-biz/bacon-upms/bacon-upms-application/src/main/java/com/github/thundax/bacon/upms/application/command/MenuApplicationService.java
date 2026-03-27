package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.api.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.api.dto.UserMenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import org.springframework.stereotype.Service;

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

    public List<MenuTreeDTO> getMenuTree(Long tenantId) {
        // 菜单树读取直接复用权限仓储结果，避免命令侧和权限侧各维护一套树装配逻辑。
        return permissionRepository.listMenus(tenantId).stream().map(this::toTreeDto).toList();
    }

    public MenuTreeDTO createMenu(Long tenantId, String menuType, String name, Long parentId, String routePath,
                                  String componentName, String icon, Integer sort, String permissionCode) {
        validateRequired(menuType, "menuType");
        validateRequired(name, "name");
        validateParent(tenantId, parentId);
        return toTreeDto(menuRepository.save(new Menu(null, tenantId, normalize(menuType), normalize(name),
                parentId == null ? 0L : parentId, normalize(routePath), normalize(componentName), normalize(icon),
                sort == null ? 0 : sort, normalize(permissionCode), List.of())));
    }

    public MenuTreeDTO updateMenu(Long tenantId, Long menuId, String menuType, String name, Long parentId, String routePath,
                                  String componentName, String icon, Integer sort, String permissionCode) {
        Menu currentMenu = menuRepository.findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        validateRequired(menuType, "menuType");
        validateRequired(name, "name");
        validateParent(tenantId, parentId);
        if (menuId.equals(parentId)) {
            throw new IllegalArgumentException("Menu parent cannot be self");
        }
        return toTreeDto(menuRepository.save(new Menu(currentMenu.getId(), tenantId, normalize(menuType), normalize(name),
                parentId == null ? 0L : parentId, normalize(routePath), normalize(componentName), normalize(icon),
                sort == null ? currentMenu.getSort() : sort, normalize(permissionCode), List.of())));
    }

    public void deleteMenu(Long tenantId, Long menuId) {
        menuRepository.findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        if (menuRepository.existsChildMenu(tenantId, menuId)) {
            throw new IllegalArgumentException("Menu has child menus: " + menuId);
        }
        menuRepository.deleteMenu(tenantId, menuId);
    }

    public MenuTreeDTO updateMenuSort(Long tenantId, Long menuId, Integer sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
        return toTreeDto(menuRepository.updateSort(tenantId, menuId, sort));
    }

    private UserMenuTreeDTO toDto(Menu menu) {
        return new UserMenuTreeDTO(menu.getId(), menu.getName(), menu.getMenuType(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(),
                menu.getChildren() == null ? List.of() : menu.getChildren().stream().map(this::toDto).toList());
    }

    private MenuTreeDTO toTreeDto(Menu menu) {
        return new MenuTreeDTO(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                menu.getChildren() == null ? List.of() : menu.getChildren().stream().map(this::toTreeDto).toList());
    }

    private void validateParent(Long tenantId, Long parentId) {
        // 菜单根节点同样用 0/NULL 语义，和部门树保持一致，减少前端和接口的分支判断。
        if (parentId == null || parentId == 0L) {
            return;
        }
        menuRepository.findMenuById(tenantId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent menu not found: " + parentId));
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
