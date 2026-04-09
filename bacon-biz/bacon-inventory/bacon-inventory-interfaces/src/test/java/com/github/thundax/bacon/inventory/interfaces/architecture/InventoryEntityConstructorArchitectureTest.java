package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：统一使用 @AllArgsConstructor，禁止显式定义构造方法")
    void inventoryEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.inventory.domain.model.entity.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.inventory"));
    }
}
