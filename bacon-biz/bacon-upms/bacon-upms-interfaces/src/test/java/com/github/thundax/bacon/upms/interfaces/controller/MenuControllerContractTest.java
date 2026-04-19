package com.github.thundax.bacon.upms.interfaces.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.command.MenuApplicationService;
import com.github.thundax.bacon.upms.application.dto.MenuTreeDTO;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class MenuControllerContractTest {

    private MenuApplicationService menuApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        menuApplicationService = mock(MenuApplicationService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new MenuController(menuApplicationService))
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

        verifyNoInteractions(menuApplicationService);
    }

    @Test
    void shouldTrimAndConvertRequestBeforeCallingApplicationService() throws Exception {
        when(menuApplicationService.createMenu(
                        eq(MenuType.CATALOG),
                        eq("Catalog"),
                        eq(MenuIdCodec.toDomain(1L)),
                        eq("/catalog"),
                        eq("CatalogPage"),
                        eq("catalog"),
                        eq("upms:menu:view")))
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
        when(menuApplicationService.updateMenu(
                        eq(MenuIdCodec.toDomain(101L)),
                        eq(MenuType.CATALOG),
                        eq("Catalog"),
                        eq(MenuIdCodec.toDomain(1L)),
                        eq("/catalog"),
                        eq("CatalogPage"),
                        eq("catalog"),
                        eq(2),
                        eq("upms:menu:edit")))
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
