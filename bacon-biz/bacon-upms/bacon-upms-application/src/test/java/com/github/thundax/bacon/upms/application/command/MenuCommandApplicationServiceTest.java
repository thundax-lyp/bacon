package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.domain.exception.MenuErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuCommandApplicationServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private IdGenerator idGenerator;

    private MenuCommandApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MenuCommandApplicationService(menuRepository, idGenerator);
    }

    @Test
    void shouldRejectMissingParentMenu() {
        MenuId parentId = MenuId.of(11L);
        when(menuRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new MenuCreateCommand(
                        MenuType.CATALOG, "Catalog", parentId, null, null, null, null)))
                .isInstanceOf(UpmsDomainException.class)
                .extracting("code")
                .isEqualTo(MenuErrorCode.PARENT_MENU_NOT_FOUND.code());
    }

    @Test
    void shouldCreateRootMenuWhenCommandIsValid() {
        when(idGenerator.nextId("menu-id")).thenReturn(1001L);
        when(menuRepository.insert(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0, Menu.class));

        MenuTreeDTO result = service.create(new MenuCreateCommand(
                MenuType.DIRECTORY, "System", null, "/sys", "SystemLayout", "system", "upms:sys:view"));

        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getMenuType()).isEqualTo("DIRECTORY");
        assertThat(result.getName()).isEqualTo("System");
        assertThat(result.getParentId()).isNull();
        assertThat(result.getRoutePath()).isEqualTo("/sys");
        assertThat(result.getComponentName()).isEqualTo("SystemLayout");
        assertThat(result.getIcon()).isEqualTo("system");
        assertThat(result.getSort()).isEqualTo(0);
        assertThat(result.getPermissionCode()).isEqualTo("upms:sys:view");
        assertThat(result.getChildren()).isEqualTo(List.of());
    }

    @Test
    void shouldRejectDeletingMenuWithChildren() {
        MenuId menuId = MenuId.of(11L);
        when(menuRepository.findById(menuId))
                .thenReturn(Optional.of(Menu.reconstruct(
                        menuId, MenuType.CATALOG, "System", null, null, null, null, 0, null, List.of())));
        when(menuRepository.existsChild(menuId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(menuId))
                .isInstanceOf(UpmsDomainException.class)
                .extracting("code")
                .isEqualTo(MenuErrorCode.MENU_HAS_CHILDREN.code());
    }
}
