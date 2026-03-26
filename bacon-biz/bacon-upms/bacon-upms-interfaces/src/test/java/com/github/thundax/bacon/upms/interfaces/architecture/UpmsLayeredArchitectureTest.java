package com.github.thundax.bacon.upms.interfaces.architecture;

import com.github.thundax.bacon.common.test.arch.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.Test;

class UpmsLayeredArchitectureTest {

    @Test
    void shouldFollowLayerDependencyDirection() {
        LayeredArchitectureRuleSupport.assertDefaultDirection("com.github.thundax.bacon.upms");
    }
}
