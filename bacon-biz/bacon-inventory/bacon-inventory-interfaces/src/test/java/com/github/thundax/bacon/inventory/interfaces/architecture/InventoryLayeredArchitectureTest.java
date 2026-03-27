package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.Test;

class InventoryLayeredArchitectureTest {

    @Test
    void shouldFollowLayerDependencyDirection() {
        LayeredArchitectureRuleSupport.assertDefaultDirection("com.github.thundax.bacon.inventory");
    }
}
