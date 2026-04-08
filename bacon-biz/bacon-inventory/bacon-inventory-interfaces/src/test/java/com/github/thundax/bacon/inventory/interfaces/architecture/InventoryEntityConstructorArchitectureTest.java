package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：仅允许一个边界构造器，且必须委托到全字段构造器")
    void inventoryEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.inventory.domain.model.entity.Inventory",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask",
                "com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.inventory"));
    }
}
