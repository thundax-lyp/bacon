package com.github.thundax.bacon.product.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductSimpleEnumArchitectureTest {

    @Test
    @DisplayName(
            "domain.model.enums：简单枚举统一成 value() -> name()、from() 走 Arrays.stream(values()) + equalsIgnoreCase + orElseThrow(...)")
    void shouldUseNameAndFromConventionForProductSimpleEnums() {
        NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(
                        "com.github.thundax.bacon.product.domain.model.enums.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.product"));
    }
}
