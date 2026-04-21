package com.github.thundax.bacon.common.core.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.Test;

class CommonPageContractArchitectureTest {

    @Test
    void shouldPlacePageContractsUnderCommonApplicationPagePackage() {
        NamingAndPlacementRuleSupport.applicationPageContractShouldUseApplicationPagePackage()
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.common"));
    }
}
