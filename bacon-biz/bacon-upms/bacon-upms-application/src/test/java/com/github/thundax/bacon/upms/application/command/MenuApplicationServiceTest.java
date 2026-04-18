package com.github.thundax.bacon.upms.application.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuApplicationServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private IdGenerator idGenerator;

    private MenuApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MenuApplicationService(menuRepository, permissionRepository, idGenerator);
    }

    @Test
    void shouldRejectMissingParentMenu() {
        MenuId parentId = MenuId.of(11L);
        when(menuRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMenu(MenuType.CATALOG, "Catalog", parentId, null, null, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Parent menu not found: " + parentId);
        verifyNoInteractions(permissionRepository);
    }
}
