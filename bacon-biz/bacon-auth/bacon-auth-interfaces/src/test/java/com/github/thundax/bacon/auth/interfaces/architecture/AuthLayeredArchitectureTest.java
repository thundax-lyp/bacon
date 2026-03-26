package com.github.thundax.bacon.auth.interfaces.architecture;

import com.github.thundax.bacon.common.test.arch.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.Test;

class AuthLayeredArchitectureTest {

    @Test
    void shouldFollowLayerDependencyDirection() {
        LayeredArchitectureRuleSupport.assertDefaultDirection("com.github.thundax.bacon.auth");
    }
}
