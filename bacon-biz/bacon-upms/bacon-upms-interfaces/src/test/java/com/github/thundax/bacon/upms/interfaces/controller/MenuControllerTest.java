package com.github.thundax.bacon.upms.interfaces.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.upms.application.command.MenuCommandApplicationService;
import com.github.thundax.bacon.upms.application.command.MenuCreateCommand;
import com.github.thundax.bacon.upms.application.command.MenuUpdateCommand;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class MenuControllerTest {

    private MenuCommandApplicationService menuCommandApplicationService;
    private com.github.thundax.bacon.upms.application.query.MenuQueryApplicationService menuQueryApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        menuCommandApplicationService = mock(MenuCommandApplicationService.class);
        menuQueryApplicationService = mock(com.github.thundax.bacon.upms.application.query.MenuQueryApplicationService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(menuCommandApplicationService, menuQueryApplicationService))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldRejectBlankMenuTypeWhenCreatingMenu() throws Exception {
        mockMvc.perform(
                        post("/upms/menus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"menuType":" ","name":"Catalog","parentId":1,"routePath":"/catalog","componentName":"CatalogPage","icon":"catalog","permissionCode":"upms:menu:view"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(menuCommandApplicationService, menuQueryApplicationService);
    }

    @Test
    void shouldTrimAndConvertRequestBeforeCallingApplicationService() throws Exception {
        when(menuCommandApplicationService.create(eq(new MenuCreateCommand(
                        MenuType.CATALOG,
                        "Catalog",
                        com.github.thundax.bacon.upms.domain.model.valueobject.MenuId.of(1L),
                        "/catalog",
                        "CatalogPage",
                        "catalog",
                        "upms:menu:view"))))
                .thenReturn(new MenuTreeDTO(
                        101L,
                        "CATALOG",
                        "Catalog",
                        1L,
                        "/catalog",
                        "CatalogPage",
                        "catalog",
                        1,
                        "upms:menu:view",
                        List.of()));

        mockMvc.perform(
                        post("/upms/menus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"menuType":" CATALOG ","name":" Catalog ","parentId":1,"routePath":" /catalog ","componentName":" CatalogPage ","icon":" catalog ","permissionCode":" upms:menu:view "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuType").value("CATALOG"))
                .andExpect(jsonPath("$.name").value("Catalog"))
                .andExpect(jsonPath("$.parentId").value(1))
                .andExpect(jsonPath("$.routePath").value("/catalog"))
                .andExpect(jsonPath("$.componentName").value("CatalogPage"))
                .andExpect(jsonPath("$.icon").value("catalog"))
                .andExpect(jsonPath("$.permissionCode").value("upms:menu:view"));
    }

    @Test
    void shouldTrimAndConvertMenuIdWhenUpdatingMenu() throws Exception {
        when(menuCommandApplicationService.update(eq(new MenuUpdateCommand(
                        com.github.thundax.bacon.upms.domain.model.valueobject.MenuId.of(101L),
                        MenuType.CATALOG,
                        "Catalog",
                        com.github.thundax.bacon.upms.domain.model.valueobject.MenuId.of(1L),
                        "/catalog",
                        "CatalogPage",
                        "catalog",
                        2,
                        "upms:menu:edit"))))
                .thenReturn(new MenuTreeDTO(
                        101L,
                        "CATALOG",
                        "Catalog",
                        1L,
                        "/catalog",
                        "CatalogPage",
                        "catalog",
                        2,
                        "upms:menu:edit",
                        List.of()));

        mockMvc.perform(
                        put("/upms/menus/{menuId}", 101L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"menuType":" CATALOG ","name":" Catalog ","parentId":1,"routePath":" /catalog ","componentName":" CatalogPage ","icon":" catalog ","sort":2,"permissionCode":" upms:menu:edit "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.sort").value(2));
    }
}
