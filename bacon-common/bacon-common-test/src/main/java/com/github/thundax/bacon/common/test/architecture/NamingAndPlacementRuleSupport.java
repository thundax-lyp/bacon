package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public final class NamingAndPlacementRuleSupport {

    private NamingAndPlacementRuleSupport() {
    }

    public static JavaClasses importDomainClasses(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);
    }

    public static ArchRule controllerShouldUseControllerNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Controller")
                .and().haveNameNotMatching(".*ProviderController$")
                .should().resideInAPackage(basePackage + ".interfaces.controller..")
                .allowEmptyShould(true)
                .because("Controller -> interfaces.controller");
    }

    public static ArchRule providerControllerShouldUseProviderControllerNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("ProviderController")
                .should().resideInAPackage(basePackage + ".interfaces.provider..")
                .allowEmptyShould(true)
                .because("ProviderController -> interfaces.provider");
    }

    public static ArchRule resolverShouldUseResolverNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Resolver")
                .should().resideInAPackage(basePackage + ".interfaces.resolver..")
                .allowEmptyShould(true)
                .because("Resolver -> interfaces.resolver");
    }

    public static ArchRule applicationServiceShouldUseApplicationServiceNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("ApplicationService")
                .should().resideInAnyPackage(
                        basePackage + ".application.command..",
                        basePackage + ".application.query..",
                        basePackage + ".application.audit..",
                        basePackage + ".application.support..")
                .allowEmptyShould(true)
                .because("ApplicationService -> application.command/query/audit, temporarily allows application.support");
    }

    public static ArchRule domainServiceShouldUseDomainServiceNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("DomainService")
                .should().resideInAPackage(basePackage + ".domain.service..")
                .allowEmptyShould(true)
                .because("DomainService -> domain.service");
    }

    public static ArchRule repositoryShouldUseRepositoryNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage(basePackage + ".domain.repository..")
                .allowEmptyShould(true)
                .because("Repository -> domain.repository");
    }

    public static ArchRule repositoryImplShouldUseRepositoryImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("RepositoryImpl")
                .should().resideInAnyPackage(
                        basePackage + ".infra.repository.impl..",
                        basePackage + ".infra.persistence.repository.impl..")
                .allowEmptyShould(true)
                .because("RepositoryImpl -> infra.repository.impl, temporarily allows infra.persistence.repository.impl");
    }

    public static ArchRule mapperShouldUseMapperNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage(basePackage + ".infra.persistence.mapper..")
                .allowEmptyShould(true)
                .because("Mapper -> infra.persistence.mapper");
    }

    public static ArchRule dataObjectShouldUseDONameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("DO")
                .should().resideInAPackage(basePackage + ".infra.persistence.dataobject..")
                .allowEmptyShould(true)
                .because("DO -> infra.persistence.dataobject");
    }

    public static ArchRule shouldNotUseDataObjectSuffix(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + "..")
                .should().haveSimpleNameEndingWith("DataObject")
                .because("持久化对象统一使用 DO 后缀，不再使用 DataObject");
    }

    public static ArchRule converterShouldUseConverterNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Converter")
                .should().resideInAPackage(basePackage + ".infra.repository.converter..")
                .allowEmptyShould(true)
                .because("Converter -> infra.repository.converter");
    }

    public static ArchRule facadeShouldUseFacadeNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Facade")
                .should().resideInAPackage(basePackage + ".api.facade..")
                .allowEmptyShould(true)
                .because("Facade -> api.facade");
    }

    public static ArchRule facadeLocalImplShouldUseFacadeLocalImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("FacadeLocalImpl")
                .should().resideInAPackage(basePackage + ".interfaces.facade..")
                .allowEmptyShould(true)
                .because("FacadeLocalImpl -> interfaces.facade");
    }

    public static ArchRule facadeRemoteImplShouldUseFacadeRemoteImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("FacadeRemoteImpl")
                .should().resideInAPackage(basePackage + ".infra.facade.remote..")
                .allowEmptyShould(true)
                .because("FacadeRemoteImpl -> infra.facade.remote");
    }
}
