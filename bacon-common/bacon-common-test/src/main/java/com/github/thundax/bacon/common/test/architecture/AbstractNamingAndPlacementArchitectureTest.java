package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractNamingAndPlacementArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = NamingAndPlacementRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    void shouldEnforceNameControllerPlacement() {
        NamingAndPlacementRuleSupport.controllerShouldUseControllerNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameProviderControllerPlacement() {
        NamingAndPlacementRuleSupport.providerControllerShouldUseProviderControllerNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameResolverPlacement() {
        NamingAndPlacementRuleSupport.resolverShouldUseResolverNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameApplicationServicePlacement() {
        NamingAndPlacementRuleSupport.applicationServiceShouldUseApplicationServiceNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameDomainServicePlacement() {
        NamingAndPlacementRuleSupport.domainServiceShouldUseDomainServiceNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameRepositoryPlacement() {
        NamingAndPlacementRuleSupport.repositoryShouldUseRepositoryNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameRepositoryMethodPrefix() {
        NamingAndPlacementRuleSupport.repositoryMethodShouldUseWhitelistedPrefix(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameRepositoryImplPlacement() {
        NamingAndPlacementRuleSupport.repositoryImplShouldUseRepositoryImplNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameRepositoryImplNoCrossImplDependency() {
        NamingAndPlacementRuleSupport.repositoryImplShouldNotDependOnOtherRepositoryImpl(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameRepositoryImplNoCrossSupportDependency() {
        NamingAndPlacementRuleSupport.repositoryImplShouldOnlyDependOnOwnPersistenceSupport(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameMapperPlacement() {
        NamingAndPlacementRuleSupport.mapperShouldUseMapperNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameDataObjectPlacement() {
        NamingAndPlacementRuleSupport.dataObjectShouldUseDONameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameDataObjectSuffix() {
        NamingAndPlacementRuleSupport.shouldNotUseDataObjectSuffix(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameConverterPlacement() {
        NamingAndPlacementRuleSupport.converterShouldUseConverterNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameCodecPlacement() {
        NamingAndPlacementRuleSupport.codecShouldUseCodecNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNamePersistenceAssemblerPlacement() {
        NamingAndPlacementRuleSupport.persistenceAssemblerShouldUsePersistenceAssemblerNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameFacadePlacement() {
        NamingAndPlacementRuleSupport.facadeShouldUseFacadeNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameFacadeRequest() {
        NamingAndPlacementRuleSupport.facadeRequestShouldUseFacadeRequestNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameFacadeResponse() {
        NamingAndPlacementRuleSupport.facadeResponseShouldUseFacadeResponseNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameFacadeLocalImplPlacement() {
        NamingAndPlacementRuleSupport.facadeLocalImplShouldUseFacadeLocalImplNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    void shouldEnforceNameFacadeRemoteImplPlacement() {
        NamingAndPlacementRuleSupport.facadeRemoteImplShouldUseFacadeRemoteImplNameAndPackage(basePackage())
                .check(classes());
    }
}
