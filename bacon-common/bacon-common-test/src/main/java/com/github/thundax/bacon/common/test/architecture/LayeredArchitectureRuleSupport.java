package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
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
    private static final String SYS_LOG_ANNOTATION = "com.github.thundax.bacon.common.log.annotation.SysLog";
    private static final List<String> TRANSACTIONAL_ANNOTATIONS =
            List.of("org.springframework.transaction.annotation.Transactional", "jakarta.transaction.Transactional");
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

    private LayeredArchitectureRuleSupport() {}

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

        // 接口层：只做 HTTP / 本地门面适配，可以使用 domain 类型，但不依赖 infra。
        interfacesShouldNotDependOnOwnInfra(basePackage).check(classes);

        // 基础设施层：只承接落库、远程调用等实现，不反向依赖 application、interfaces。
        infraShouldNotDependOnApplicationOrInterfaces(basePackage).check(classes);
    }

    public static ArchRule domainShouldNotDependOnOuterLayers(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".application..", basePackage + ".interfaces..", basePackage + ".infra..")
                .because("domain 不依赖 application、interfaces、infra");
    }

    public static ArchRule applicationShouldNotDependOnInterfacesOrOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".interfaces..", basePackage + ".infra..")
                .because("application 不依赖 interfaces、infra");
    }

    public static ArchRule interfacesShouldNotDependOnOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".infra..")
                .because("interfaces 可以使用 domain 类型，但不依赖 infra");
    }

    public static ArchRule infraShouldNotDependOnApplicationOrInterfaces(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".application..", basePackage + ".interfaces..")
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
                packageName -> packageName.startsWith(ROOT_PACKAGE + ".") && packageName.contains(".infra."),
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

    public static ArchRule sysLogShouldOnlyAppearInInterfacesController(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".interfaces.controller..",
                List.of(SYS_LOG_ANNOTATION),
                "@SysLog",
                "@SysLog 只能出现在 interfaces.controller");
    }

    public static ArchRule transactionalShouldOnlyAppearInApplication(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".application..",
                TRANSACTIONAL_ANNOTATIONS,
                "@Transactional",
                "@Transactional 默认只允许出现在 application");
    }

    public static ArchRule infraShouldOnlyDependOnDomainRepositoryAsImplementation(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".infra..")
                .and()
                .resideOutsideOfPackage(basePackage + ".infra.repository.impl..")
                .should(new ArchCondition<>("depend on domain.repository") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            if (targetPackage.startsWith(basePackage + ".domain.repository.")) {
                                events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
                            }
                        }
                    }
                })
                .because("infra 只能作为实现层依赖 domain.repository");
    }

    public static ArchRule domainEntityCreateShouldOnlyBeCalledByApplication(String basePackage) {
        return noClassesOutsidePackageShouldCallDomainEntityStaticFactory(
                basePackage, basePackage + ".application..", "create", "domain entity 只能由 application 创建");
    }

    public static ArchRule domainEntityReconstructShouldOnlyBeCalledByInfra(String basePackage) {
        return noClassesOutsidePackageShouldCallDomainEntityStaticFactory(
                basePackage, basePackage + ".infra..", "reconstruct", "domain entity 只能由 infra 重建");
    }

    public static ArchRule applicationPublicMethodsShouldNotUseProtocolModels(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".application..")
                .should(new ArchCondition<>("keep public application method contracts away from protocol models") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)
                                    || !method.getModifiers().contains(JavaModifier.PUBLIC)) {
                                continue;
                            }
                            for (JavaClass parameterType : method.getRawParameterTypes()) {
                                if (isForbiddenApplicationBoundaryType(basePackage, parameterType)) {
                                    events.add(SimpleConditionEvent.violated(
                                            method,
                                            method.getFullName() + " uses forbidden parameter type "
                                                    + parameterType.getFullName()));
                                }
                            }
                        }
                    }
                })
                .because(
                        "application public methods must consume stable application/domain contracts, not protocol models");
    }

    public static ArchRule applicationServicesShouldUseAssemblersForDtoMapping(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAnyPackage(
                        basePackage + ".application.command..",
                        basePackage + ".application.query..",
                        basePackage + ".application.audit..")
                .and()
                .haveSimpleNameEndingWith("ApplicationService")
                .should(new ArchCondition<>("avoid local dto mapping helpers and direct dto construction") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : item.getMethods()) {
                            if (method.getOwner().equals(item) && "toDto".equals(method.getName())) {
                                events.add(
                                        SimpleConditionEvent.violated(
                                                method,
                                                method.getFullName()
                                                        + " declares local dto mapping; move mapping to application.assembler"));
                            }
                        }
                        for (JavaConstructorCall constructorCall : item.getConstructorCallsFromSelf()) {
                            JavaClass targetOwner = constructorCall.getTargetOwner();
                            if (targetOwner.getPackageName().startsWith(basePackage + ".api.dto.")) {
                                events.add(SimpleConditionEvent.violated(
                                        item,
                                        constructorCall.getDescription()
                                                + " constructs api dto directly; use application.assembler instead"));
                            }
                        }
                    }
                })
                .because("application service 应通过 application.assembler 完成 DTO 映射，而不是本地 toDto 或直接 new DTO");
    }

    private static boolean isForbiddenDomainTechnologyPackage(String packageName) {
        return DOMAIN_FORBIDDEN_TECH_PACKAGES.stream().anyMatch(packageName::startsWith);
    }

    private static ArchRule noClassesOutsidePackageShouldUseAnnotations(
            String allowedPackage, List<String> annotationNames, String annotationDescription, String because) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideOutsideOfPackage(allowedPackage)
                .should(new ArchCondition<>("use " + annotationDescription) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (hasAnyAnnotation(item.getAnnotations(), annotationNames)) {
                            events.add(SimpleConditionEvent.violated(
                                    item, item.getName() + " is annotated with " + annotationDescription));
                        }
                        for (JavaCodeUnit codeUnit : item.getCodeUnits()) {
                            if (hasAnyAnnotation(codeUnit.getAnnotations(), annotationNames)) {
                                events.add(SimpleConditionEvent.violated(
                                        codeUnit,
                                        codeUnit.getFullName() + " is annotated with " + annotationDescription));
                            }
                        }
                    }
                })
                .because(because);
    }

    private static ArchRule noClassesOutsidePackageShouldCallDomainEntityStaticFactory(
            String basePackage, String allowedPackage, String methodName, String because) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideOutsideOfPackage(allowedPackage)
                .should(new ArchCondition<>("call domain entity static method " + methodName) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethodCall methodCall : item.getMethodCallsFromSelf()) {
                            JavaClass targetOwner = methodCall.getTargetOwner();
                            if (targetOwner.getPackageName().startsWith(basePackage + ".domain.model.entity.")
                                    && methodName.equals(methodCall.getName())) {
                                events.add(SimpleConditionEvent.violated(item, methodCall.getDescription()));
                            }
                        }
                    }
                })
                .because(because);
    }

    private static boolean hasAnyAnnotation(
            Iterable<? extends JavaAnnotation<?>> annotations, List<String> annotationNames) {
        for (JavaAnnotation<?> annotation : annotations) {
            if (annotationNames.contains(annotation.getRawType().getName())) {
                return true;
            }
        }
        return false;
    }

    private static ArchRule noDirectDependencies(
            String sourcePackage,
            Predicate<String> forbiddenTargetPackage,
            String forbiddenDescription,
            String because) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(sourcePackage)
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

    private static boolean isForbiddenApplicationBoundaryType(String basePackage, JavaClass parameterType) {
        String packageName = parameterType.getPackageName();
        String simpleName = parameterType.getSimpleName();
        if (packageName.startsWith(basePackage + ".interfaces.dto.")
                || packageName.startsWith(basePackage + ".interfaces.response.")
                || packageName.startsWith(basePackage + ".interfaces.vo.")) {
            return true;
        }
        return packageName.startsWith(basePackage + ".api.dto.")
                && (simpleName.endsWith("QueryDTO") || simpleName.endsWith("PageQueryDTO"));
    }
}
