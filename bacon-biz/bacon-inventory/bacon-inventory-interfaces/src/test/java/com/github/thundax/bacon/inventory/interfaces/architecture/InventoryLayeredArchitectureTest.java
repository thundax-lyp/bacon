package com.github.thundax.bacon.inventory.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.AbstractLayeredArchitectureTest;
import com.github.thundax.bacon.common.test.architecture.LayeredArchitectureRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryLayeredArchitectureTest extends AbstractLayeredArchitectureTest {

    @Override
    protected String basePackage() {
        return "com.github.thundax.bacon.inventory";
    }

    @Test
    @DisplayName("domain entity 只能由 application 创建")
    void shouldRestrictDomainEntityCreateToApplication() {
        LayeredArchitectureRuleSupport.domainEntityCreateShouldOnlyBeCalledByApplication(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("domain entity 只能由 infra 重建")
    void shouldRestrictDomainEntityReconstructToInfra() {
        LayeredArchitectureRuleSupport.domainEntityReconstructShouldOnlyBeCalledByInfra(basePackage())
                .check(classes());
    }
}
