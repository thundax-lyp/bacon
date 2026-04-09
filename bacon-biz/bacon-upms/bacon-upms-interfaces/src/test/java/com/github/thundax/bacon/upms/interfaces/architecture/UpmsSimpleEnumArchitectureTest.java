package com.github.thundax.bacon.upms.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpmsSimpleEnumArchitectureTest {

    @Test
    @DisplayName(
            "domain.model.enums：简单枚举统一成 value() -> name()、from() 走 Arrays.stream(values()) + equalsIgnoreCase + orElseThrow(...)")
    void upmsSimpleEnumsShouldUseNameAndFromConvention() {
        NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(
                        "com.github.thundax.bacon.upms.domain.model.enums.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.upms"));
    }
}
