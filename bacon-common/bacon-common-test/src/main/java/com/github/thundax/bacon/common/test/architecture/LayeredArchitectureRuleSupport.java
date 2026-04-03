package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.util.List;
import java.util.function.Predicate;

public final class LayeredArchitectureRuleSupport {

    private static final String ROOT_PACKAGE = "com.github.thundax.bacon";
    private static final List<String> DOMAIN_FORBIDDEN_TECH_PACKAGES = List.of(
            "org.springframework.web.",
            "org.springframework.http.",
            "org.springframework.web.client.",
            "org.springframework.cloud.openfeign.",
            "feign.",
            "org.apache.ibatis.",
            "org.mybatis.",
            "com.baomidou.mybatisplus.",
            "org.springframework.data.redis.",
            "org.redisson.",
            "org.springframework.amqp.",
            "org.springframework.kafka.",
            "org.apache.rocketmq.");

    private LayeredArchitectureRuleSupport() {
    }

    public static JavaClasses importDomainClasses(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);
    }

    public static void assertDefaultDirection(String basePackage) {
        JavaClasses classes = importDomainClasses(basePackage);

        // 领域层：只承载业务规则，不依赖 application、interfaces、infra。
        domainShouldNotDependOnOuterLayers(basePackage).check(classes);

        // 应用层：只负责编排业务用例，不依赖 interfaces、infra。
        applicationShouldNotDependOnInterfacesOrOwnInfra(basePackage).check(classes);

        // 接口层：只做 HTTP / 本地门面适配，不直接依赖 domain、infra。
        interfacesShouldNotDependOnDomainOrOwnInfra(basePackage).check(classes);

        // 基础设施层：只承接落库、远程调用等实现，不反向依赖 application、interfaces。
        infraShouldNotDependOnApplicationOrInterfaces(basePackage).check(classes);
    }

    public static ArchRule domainShouldNotDependOnOuterLayers(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .because("domain 不依赖 application、interfaces、infra");
    }

    public static ArchRule applicationShouldNotDependOnInterfacesOrOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .because("application 不依赖 interfaces、infra");
    }

    public static ArchRule interfacesShouldNotDependOnDomainOrOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".domain..",
                        basePackage + ".infra..")
                .because("interfaces 不依赖 domain、infra");
    }

    public static ArchRule infraShouldNotDependOnApplicationOrInterfaces(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".infra..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..")
                .because("infra 不依赖 application、interfaces");
    }

    public static ArchRule interfacesShouldNotDependOnPersistenceMapper(String basePackage) {
        return noDirectDependencies(
                basePackage + ".interfaces..",
                packageName -> packageName.startsWith(basePackage + ".infra.persistence.mapper."),
                "infra.persistence.mapper",
                "interfaces 不得直接依赖 infra.persistence.mapper");
    }

    public static ArchRule interfacesShouldNotDependOnOtherDomainInfra(String basePackage) {
        return noDirectDependencies(
                basePackage + ".interfaces..",
                packageName -> packageName.startsWith(ROOT_PACKAGE + ".")
                        && packageName.contains(".infra.")
                        && !packageName.startsWith(basePackage + "."),
                "其他业务域的 infra",
                "interfaces 不得直接依赖其他业务域的 infra");
    }

    public static ArchRule applicationShouldNotDependOnAnyDomainInfra(String basePackage) {
        return noDirectDependencies(
                basePackage + ".application..",
                packageName -> packageName.startsWith(ROOT_PACKAGE + ".")
                        && packageName.contains(".infra."),
                "infra",
                "application 不得依赖本域或他域的 infra");
    }

    public static ArchRule domainShouldNotDependOnTechnicalPackages(String basePackage) {
        return noDirectDependencies(
                basePackage + ".domain..",
                LayeredArchitectureRuleSupport::isForbiddenDomainTechnologyPackage,
                "Spring MVC、MyBatis、HTTP client、Redis、MQ 等技术包",
                "domain 不得依赖 Spring MVC、MyBatis、HTTP client、Redis、MQ 等技术包");
    }

    private static boolean isForbiddenDomainTechnologyPackage(String packageName) {
        return DOMAIN_FORBIDDEN_TECH_PACKAGES.stream().anyMatch(packageName::startsWith);
    }

    private static ArchRule noDirectDependencies(
            String sourcePackage,
            Predicate<String> forbiddenTargetPackage,
            String forbiddenDescription,
            String because) {
        return ArchRuleDefinition.noClasses()
                .that().resideInAPackage(sourcePackage)
                .should(new ArchCondition<>("directly depend on " + forbiddenDescription) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            if (forbiddenTargetPackage.test(targetPackage)) {
                                events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
                            }
                        }
                    }
                })
                .because(because);
    }
}
