package com.github.thundax.bacon.order.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.Test;

class OrderLayeredArchitectureTest {

    @Test
    void shouldFollowLayerDependencyDirection() {
        LayeredArchitectureRuleSupport.assertDefaultDirection("com.github.thundax.bacon.order");
    }
}
