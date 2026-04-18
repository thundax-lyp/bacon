package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.MenuType;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import org.junit.jupiter.api.Test;

class MenuTest {

    @Test
    void shouldRejectSelfAsParentWhenCreatingMenu() {
        assertThatThrownBy(() -> Menu.create(
                        MenuId.of(101L),
                        MenuType.CATALOG,
                        "Catalog",
                        MenuId.of(101L),
                        "/catalog",
                        "CatalogPage",
                        "catalog",
                        null))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Menu parent cannot be self");
    }

    @Test
    void shouldRejectSelfAsParentWhenUpdatingMenu() {
        Menu menu =
                Menu.create(MenuId.of(101L), MenuType.CATALOG, "Catalog", null, "/catalog", "CatalogPage", "catalog", null);

        assertThatThrownBy(() -> menu.moveUnder(MenuId.of(101L)))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Menu parent cannot be self");
    }

    @Test
    void shouldDefaultSortWhenCreatingMenu() {
        Menu menu =
                Menu.create(MenuId.of(101L), MenuType.CATALOG, "Catalog", null, "/catalog", "CatalogPage", "catalog", null);

        assertThat(menu.getSort()).isEqualTo(0);
    }

    @Test
    void shouldRejectNegativeSortWhenSortingMenu() {
        Menu menu =
                Menu.create(MenuId.of(101L), MenuType.CATALOG, "Catalog", null, "/catalog", "CatalogPage", "catalog", null);

        assertThatThrownBy(() -> menu.sort(-1))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("Menu sort must be greater than or equal to 0");
    }

    @Test
    void shouldAddAndRemoveChild() {
        Menu parent =
                Menu.create(MenuId.of(101L), MenuType.CATALOG, "Catalog", null, "/catalog", "CatalogPage", "catalog", null);
        Menu child =
                Menu.create(MenuId.of(102L), MenuType.MENU, "Child", parent.getId(), "/catalog/child", "ChildPage", "child", null);

        parent.addChild(child);
        assertThat(parent.getChildren()).containsExactly(child);

        parent.removeChild(child);
        assertThat(parent.getChildren()).isEmpty();
    }
}
