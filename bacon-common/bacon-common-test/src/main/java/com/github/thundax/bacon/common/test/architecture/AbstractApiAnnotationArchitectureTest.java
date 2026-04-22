package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractApiAnnotationArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = ApiAnnotationArchitectureRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    void shouldEnforceAnnoCommonClassBaseRequired() {
        ApiAnnotationArchitectureRuleSupport.commonClassBaseAnnotationsRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoCommonMethodMappingRequired() {
        ApiAnnotationArchitectureRuleSupport.commonHttpMappingRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoCommonRequestParamValidRequired() {
        ApiAnnotationArchitectureRuleSupport.commonRequestParamValidRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoBffClassWrappedRequired() {
        ApiAnnotationArchitectureRuleSupport.bffWrappedApiControllerRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoBffMethodOperationRequired() {
        ApiAnnotationArchitectureRuleSupport.bffOperationRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoBffPermissionRequired() {
        ApiAnnotationArchitectureRuleSupport.bffPermissionRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoCallbackOperationRequired() {
        ApiAnnotationArchitectureRuleSupport.callbackOperationRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoCallbackPermissionForbidden() {
        ApiAnnotationArchitectureRuleSupport.callbackPermissionForbidden(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoCallbackSysLogForbidden() {
        ApiAnnotationArchitectureRuleSupport.callbackSysLogForbidden(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoProviderPathPrefixRequired() {
        ApiAnnotationArchitectureRuleSupport.providerPathPrefixRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoProviderMethodOperationRequired() {
        ApiAnnotationArchitectureRuleSupport.providerOperationRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoProviderPermissionForbidden() {
        ApiAnnotationArchitectureRuleSupport.providerPermissionForbidden(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoFacadeEndpointAnnotationForbidden() {
        ApiAnnotationArchitectureRuleSupport.facadeEndpointAnnotationsForbidden(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoFacadeSecurityAnnotationForbidden() {
        ApiAnnotationArchitectureRuleSupport.facadeSecurityAnnotationsForbidden(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoExceptionAnnotationScopeRequired() {
        ApiAnnotationArchitectureRuleSupport.exceptionAnnotationScopeRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoExceptionAnnotationBucketEnumRequired() {
        ApiAnnotationArchitectureRuleSupport.exceptionAnnotationBucketEnumRequired(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceAnnoExceptionAnnotationAppliesRulesOnly() {
        ApiAnnotationArchitectureRuleSupport.exceptionAnnotationAppliesPermissionRulesOnly(basePackage())
                .check(classes());
    }
}
