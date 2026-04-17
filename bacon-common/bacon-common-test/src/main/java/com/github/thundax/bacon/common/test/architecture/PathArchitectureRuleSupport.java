package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

public final class PathArchitectureRuleSupport {

    private PathArchitectureRuleSupport() {}

    public static JavaClasses importDomainClasses(String basePackage) {
        return NamingAndPlacementRuleSupport.importDomainClasses(basePackage);
    }

    public static ArchRule controllerRequestMappingShouldUseDomainPrefix(String basePackage) {
        return NamingAndPlacementRuleSupport.controllerRequestMappingShouldUseDomainPrefix(basePackage);
    }

    public static ArchRule providerControllerRequestMappingShouldUseDomainPrefix(String basePackage) {
        return NamingAndPlacementRuleSupport.providerControllerRequestMappingShouldUseDomainPrefix(basePackage);
    }
}
