package com.github.thundax.bacon.upms.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpmsEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：统一使用 @AllArgsConstructor，禁止显式定义构造方法")
    void upmsEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.upms.domain.model.entity.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.upms"));
    }
}
