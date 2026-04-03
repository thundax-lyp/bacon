package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public final class LayeredArchitectureRuleSupport {

    private LayeredArchitectureRuleSupport() {
    }

    public static void assertDefaultDirection(String basePackage) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);

        // 领域层：只承载业务规则，不依赖 application、interfaces、infra。
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .because("domain 不依赖 application、interfaces、infra")
                .check(classes);

        // 应用层：只负责编排业务用例，不依赖 interfaces、infra。
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .because("application 不依赖 interfaces、infra")
                .check(classes);

        // 接口层：只做 HTTP / 本地门面适配，不直接依赖 domain、infra。
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".domain..",
                        basePackage + ".infra..")
                .because("interfaces 不依赖 domain、infra")
                .check(classes);

        // 基础设施层：只承接落库、远程调用等实现，不反向依赖 application、interfaces。
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".infra..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..")
                .because("infra 不依赖 application、interfaces")
                .check(classes);
    }
}
