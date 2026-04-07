package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.Test;

class InventoryEntityConstructorArchitectureTest {

    @Test
    void inventoryAuditDeadLetterShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.inventory"));
    }
}
