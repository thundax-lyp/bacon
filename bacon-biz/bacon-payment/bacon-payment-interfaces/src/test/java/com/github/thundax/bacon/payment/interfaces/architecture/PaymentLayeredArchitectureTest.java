package com.github.thundax.bacon.payment.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.Test;

class PaymentLayeredArchitectureTest {

    @Test
    void shouldFollowLayerDependencyDirection() {
        LayeredArchitectureRuleSupport.assertDefaultDirection("com.github.thundax.bacon.payment");
    }
}
