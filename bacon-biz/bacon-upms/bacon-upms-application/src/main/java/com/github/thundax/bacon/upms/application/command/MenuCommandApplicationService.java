package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.application.assembler.MenuAssembler;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.domain.exception.MenuErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuCommandApplicationService {

    private static final String MENU_ID_BIZ_TAG = "menu-id";

    private final MenuRepository menuRepository;
    private final IdGenerator idGenerator;

    public MenuCommandApplicationService(MenuRepository menuRepository, IdGenerator idGenerator) {
        this.menuRepository = menuRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public MenuTreeDTO create(MenuCreateCommand command) {
        validateRequired(command.name(), "name");
        validateParent(command.parentId());
        return toTreeDto(menuRepository.insert(Menu.create(
                MenuIdCodec.toDomain(idGenerator.nextId(MENU_ID_BIZ_TAG)),
                command.menuType(),
                command.name(),
                command.parentId(),
                command.routePath(),
                command.componentName(),
                command.icon(),
                command.permissionCode())));
    }

    @Transactional
    public MenuTreeDTO update(MenuUpdateCommand command) {
        Menu currentMenu = requireMenu(command.menuId());
        validateRequired(command.name(), "name");
        validateParent(command.parentId());
        currentMenu.retypeAs(command.menuType());
        currentMenu.rename(command.name());
        currentMenu.moveUnder(command.parentId());
        currentMenu.routeTo(command.routePath());
        currentMenu.renderWith(command.componentName());
        currentMenu.showIcon(command.icon());
        currentMenu.bindPermission(command.permissionCode());
        if (command.sort() != null) {
            currentMenu.sort(command.sort());
        }
        return toTreeDto(menuRepository.update(currentMenu));
    }

    @Transactional
    public void delete(MenuId menuId) {
        requireMenu(menuId);
        if (menuRepository.existsChild(menuId)) {
            throw new UpmsDomainException(MenuErrorCode.MENU_HAS_CHILDREN);
        }
        menuRepository.delete(menuId);
    }

    @Transactional
    public MenuTreeDTO updateSort(MenuSortUpdateCommand command) {
        if (command.sort() == null) {
            throw new UpmsDomainException(MenuErrorCode.MENU_SORT_REQUIRED);
        }
        Menu currentMenu = requireMenu(command.menuId());
        currentMenu.sort(command.sort());
        return toTreeDto(menuRepository.update(currentMenu));
    }

    private MenuTreeDTO toTreeDto(Menu menu) {
        return MenuAssembler.toTreeDto(menu);
    }

    private Menu requireMenu(MenuId menuId) {
        return menuRepository
                .findById(menuId)
                .orElseThrow(() -> new UpmsDomainException(MenuErrorCode.MENU_NOT_FOUND));
    }

    private void validateParent(MenuId parentId) {
        // 菜单根节点统一用 null parentId 语义，避免和正数型 ID 约束冲突。
        if (parentId == null) {
            return;
        }
        menuRepository
                .findById(parentId)
                .orElseThrow(() -> new UpmsDomainException(MenuErrorCode.PARENT_MENU_NOT_FOUND));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new UpmsDomainException(MenuErrorCode.MENU_REQUIRED_FIELD_BLANK);
        }
    }
}
