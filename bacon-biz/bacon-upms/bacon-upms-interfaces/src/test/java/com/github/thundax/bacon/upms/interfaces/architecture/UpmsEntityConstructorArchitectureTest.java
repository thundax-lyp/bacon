package com.github.thundax.bacon.upms.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpmsEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：仅允许一个边界构造器，且必须委托到全字段构造器")
    void upmsEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.upms.domain.model.entity.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.upms"));
    }
}
