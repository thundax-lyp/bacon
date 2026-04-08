package com.github.thundax.bacon.auth.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthSimpleEnumArchitectureTest {

    @Test
    @DisplayName("domain.model.enums：简单枚举统一成 value() -> name()、from() 走 Arrays.stream(values()) + equalsIgnoreCase + orElseThrow(...)")
    void authSimpleEnumsShouldUseNameAndFromConvention() {
        NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(
                "com.github.thundax.bacon.auth.domain.model.enums.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.auth"));
    }
}
