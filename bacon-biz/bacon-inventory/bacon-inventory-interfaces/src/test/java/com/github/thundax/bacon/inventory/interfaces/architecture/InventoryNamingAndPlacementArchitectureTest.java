package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.AbstractNamingAndPlacementArchitectureTest;
import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryNamingAndPlacementArchitectureTest extends AbstractNamingAndPlacementArchitectureTest {

    @Override
    protected String basePackage() {
        return "com.github.thundax.bacon.inventory";
    }

    @Test
    @DisplayName("Inventory Facade：方法签名统一使用单个 FacadeRequest 和 FacadeResponse")
    void shouldUseFacadeRequestAndFacadeResponseForInventoryFacades() {
        NamingAndPlacementRuleSupport.facadeMethodShouldUseFacadeRequestAndResponse(basePackage())
                .check(classes());
    }
}
