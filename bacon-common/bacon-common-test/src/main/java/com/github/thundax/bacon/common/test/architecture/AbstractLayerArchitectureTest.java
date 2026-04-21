package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractLayerArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = LayerArchitectureRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    void shouldEnforceDefaultLayerDirection() {
        LayerArchitectureRuleSupport.assertDefaultDirection(basePackage());
    }

    @Test
    void shouldKeepInterfacesAwayFromPersistenceMapper() {
        LayerArchitectureRuleSupport.interfacesShouldNotDependOnPersistenceMapper(basePackage()).check(classes());
    }

    @Test
    void shouldEnforceLayerCrossDomainFacadeOnlyForOtherDomainApplication() {
        LayerArchitectureRuleSupport.controllerAndProviderShouldNotDependOnOtherDomainApplication(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerCrossDomainFacadeOnlyForOtherDomainInfra() {
        LayerArchitectureRuleSupport.interfacesShouldNotDependOnOtherDomainInfra(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerInterfacesDependencyWhitelist() {
        LayerArchitectureRuleSupport.interfacesShouldOnlyDependOnWhitelistedPackages(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerFacadeSignatureModel() {
        NamingAndPlacementRuleSupport.facadeMethodShouldUseFacadeRequestAndResponse(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerFacadeSingleRequest() {
        NamingAndPlacementRuleSupport.facadeMethodShouldUseSingleFacadeRequest(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerFacadeResponseOnly() {
        NamingAndPlacementRuleSupport.facadeMethodShouldUseFacadeResponse(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceLayerPersistenceAssemblerPublicMethods() {
        NamingAndPlacementRuleSupport.persistenceAssemblerPublicMethodsShouldUseToDomainAndToDataObject(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepApplicationAwayFromInfra() {
        LayerArchitectureRuleSupport.applicationShouldNotDependOnAnyDomainInfra(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepApiAwayFromDomain() {
        LayerArchitectureRuleSupport.apiShouldNotDependOnAnyDomain(basePackage()).check(classes());
    }

    @Test
    void shouldKeepDomainAwayFromTechnicalPackages() {
        LayerArchitectureRuleSupport.domainShouldNotDependOnTechnicalPackages(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepSysLogOnlyInInterfacesController() {
        LayerArchitectureRuleSupport.sysLogShouldOnlyAppearInInterfacesController(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepTransactionalOnlyInApplication() {
        LayerArchitectureRuleSupport.transactionalShouldOnlyAppearInApplication(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepDomainRepositoryDependencyInsideInfraRepositoryImpl() {
        LayerArchitectureRuleSupport.infraShouldOnlyDependOnDomainRepositoryAsImplementation(basePackage())
                .check(classes());
    }

    @Test
    void shouldAllowOnlyApplicationToCallDomainEntityCreate() {
        LayerArchitectureRuleSupport.domainEntityCreateShouldOnlyBeCalledByApplication(basePackage())
                .check(classes());
    }

    @Test
    void shouldAllowOnlyInfraToCallDomainEntityReconstruct() {
        LayerArchitectureRuleSupport.domainEntityReconstructShouldOnlyBeCalledByInfra(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepApplicationPublicMethodContractsStable() {
        LayerArchitectureRuleSupport.applicationPublicMethodsShouldNotUseProtocolModels(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepDtoMappingInsideApplicationAssembler() {
        LayerArchitectureRuleSupport.applicationServicesShouldUseAssemblersForDtoMapping(basePackage())
                .check(classes());
    }

    @Test
    void shouldKeepApplicationAndInfraRepositoryAwayFromIllegalArgumentException() {
        LayerArchitectureRuleSupport.applicationAndInfraRepositoryShouldNotUseIllegalArgumentException(basePackage())
                .check(classes());
    }
}
