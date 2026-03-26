package com.github.thundax.bacon.common.test.arch;

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

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .check(classes);

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".interfaces..",
                        basePackage + ".infra..")
                .check(classes);

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".domain..",
                        basePackage + ".infra..")
                .check(classes);

        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(basePackage + ".infra..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".interfaces..")
                .check(classes);
    }
}
