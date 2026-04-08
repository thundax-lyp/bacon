package com.github.thundax.bacon.auth.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：仅允许一个边界构造器，且必须委托到全字段构造器")
    void authEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.auth.domain.model.entity.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.auth"));
    }
}
