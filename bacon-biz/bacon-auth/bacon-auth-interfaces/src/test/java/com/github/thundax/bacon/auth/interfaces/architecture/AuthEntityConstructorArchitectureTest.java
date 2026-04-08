package com.github.thundax.bacon.auth.interfaces.architecture;

import com.github.thundax.bacon.common.test.architecture.NamingAndPlacementRuleSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthEntityConstructorArchitectureTest {

    @Test
    @DisplayName("domain.model.entity：统一使用 @AllArgsConstructor，禁止显式定义构造方法")
    void authEntitiesShouldUseSingleExplicitBoundaryConstructor() {
        NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(
                "com.github.thundax.bacon.auth.domain.model.entity.*")
                .check(NamingAndPlacementRuleSupport.importDomainClasses("com.github.thundax.bacon.auth"));
    }
}
