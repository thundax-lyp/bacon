package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class LayerArchitectureRuleSupport {

    private static final String ROOT_PACKAGE = "com.github.thundax.bacon";
    private static final String SYS_LOG_ANNOTATION = "com.github.thundax.bacon.common.log.annotation.SysLog";
    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";
    private static final String FEIGN_CLIENT_ANNOTATION = "org.springframework.cloud.openfeign.FeignClient";
    private static final String MYBATIS_MAPPER_ANNOTATION = "org.apache.ibatis.annotations.Mapper";
    private static final List<String> MYBATIS_PLUS_TABLE_ANNOTATIONS =
            List.of("com.baomidou.mybatisplus.annotation.TableName", "com.baomidou.mybatisplus.annotation.TableField");
    private static final List<String> TRANSACTIONAL_ANNOTATIONS =
            List.of("org.springframework.transaction.annotation.Transactional", "jakarta.transaction.Transactional");
    private static final Set<String> BUSINESS_DOMAINS =
            Set.of("auth", "inventory", "order", "payment", "storage", "upms");
    private static final Set<String> BUSINESS_LAYERS = Set.of("api", "interfaces", "application", "domain", "infra");
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

    private LayerArchitectureRuleSupport() {}

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

        // 契约层：api 只承载本域契约，不反向耦合其他业务域实现。
        apiShouldNotDependOnAnyOtherDomainModules(basePackage).check(classes);
    }

    private static ArchRule domainShouldNotDependOnOuterLayers(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".application..", basePackage + ".interfaces..", basePackage + ".infra..")
                .because("domain 不依赖 application、interfaces、infra");
    }

    private static ArchRule applicationShouldNotDependOnInterfacesOrOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".interfaces..", basePackage + ".infra..")
                .because("application 不依赖 interfaces、infra");
    }

    private static ArchRule interfacesShouldNotDependOnOwnInfra(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".infra..")
                .because("interfaces 可以使用 domain 类型，但不依赖 infra");
    }

    private static ArchRule infraShouldNotDependOnApplicationOrInterfaces(String basePackage) {
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

    public static ArchRule interfacesShouldOnlyDependOnWhitelistedPackages(String basePackage) {
        String sourceDomain = extractDomainSegment(basePackage);
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should(new ArchCondition<>("depend on non-whitelisted business packages") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            if (!targetPackage.startsWith(ROOT_PACKAGE + ".")) {
                                continue;
                            }
                            if (isWhitelistedInterfacesDependency(basePackage, sourceDomain, item, targetPackage)) {
                                continue;
                            }
                            events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
                        }
                    }
                })
                .because(
                        "RULE LAYER_INTERFACES_DEPENDENCY_WHITELIST: interfaces must depend only on "
                                + "whitelisted packages");
    }

    public static ArchRule controllerAndProviderShouldNotDependOnOtherDomainApplication(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage(basePackage + ".interfaces.controller..", basePackage + ".interfaces.provider..")
                .should(new ArchCondition<>("directly depend on other domain application") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                            String targetPackage = dependency.getTargetClass().getPackageName();
                            if (targetPackage.startsWith(ROOT_PACKAGE + ".")
                                    && targetPackage.contains(".application.")
                                    && !targetPackage.startsWith(basePackage + ".")) {
                                events.add(SimpleConditionEvent.violated(item, dependency.getDescription()));
                            }
                        }
                    }
                })
                .because("RULE LAYER_CROSS_DOMAIN_FACADE_ONLY: controller/provider must not depend on other-domain application");
    }

    public static ArchRule applicationShouldNotDependOnAnyDomainInfra(String basePackage) {
        return noDirectDependencies(
                basePackage + ".application..",
                packageName -> packageName.startsWith(ROOT_PACKAGE + ".") && packageName.contains(".infra."),
                "infra",
                "application 不得依赖本域或他域的 infra");
    }

    public static ArchRule apiShouldNotDependOnAnyDomain(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".api..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(ROOT_PACKAGE + "..domain..")
                .because("api 不得依赖本域或他域的 domain");
    }

    public static ArchRule apiShouldNotDependOnAnyOtherDomainModules(String basePackage) {
        String sourceDomain = extractDomainSegment(basePackage);
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + ".api..")
                .should()
                .dependOnClassesThat(new DescribedPredicate<>("reside in other domain modules") {
                    @Override
                    public boolean test(JavaClass input) {
                        return isOtherDomainBusinessModulePackage(sourceDomain, input.getPackageName());
                    }
                })
                .because("api is contract base and must not depend on other domain modules");
    }

    public static ArchRule domainShouldNotDependOnTechnicalPackages(String basePackage) {
        return noDirectDependencies(
                basePackage + ".domain..",
                LayerArchitectureRuleSupport::isForbiddenDomainTechnologyPackage,
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

    public static ArchRule restControllerShouldOnlyAppearInInterfacesControllerAndProvider(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".interfaces.controller..",
                basePackage + ".interfaces.provider..",
                List.of(REST_CONTROLLER_ANNOTATION),
                "@RestController",
                "RULE LAYER_ANNOTATION_PLACEMENT_WHITELIST: @RestController only in interfaces.controller/provider");
    }

    public static ArchRule feignClientShouldOnlyAppearInInfraFacadeRemote(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".infra.facade.remote..",
                List.of(FEIGN_CLIENT_ANNOTATION),
                "@FeignClient",
                "RULE LAYER_ANNOTATION_PLACEMENT_WHITELIST: @FeignClient only in infra.facade.remote");
    }

    public static ArchRule mapperShouldOnlyAppearInInfraPersistenceMapper(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".infra.persistence.mapper..",
                List.of(MYBATIS_MAPPER_ANNOTATION),
                "@Mapper",
                "RULE LAYER_ANNOTATION_PLACEMENT_WHITELIST: @Mapper only in infra.persistence.mapper");
    }

    public static ArchRule tableAnnotationsShouldOnlyAppearInInfraPersistenceDataobject(String basePackage) {
        return noClassesOutsidePackageShouldUseAnnotations(
                basePackage + ".infra.persistence.dataobject..",
                MYBATIS_PLUS_TABLE_ANNOTATIONS,
                "@TableName/@TableField",
                "RULE LAYER_ANNOTATION_PLACEMENT_WHITELIST: @TableName/@TableField only in "
                        + "infra.persistence.dataobject");
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
                        basePackage + ".application.audit..",
                        basePackage + ".application.support..")
                .should(new ArchCondition<>("avoid local dto/response mapping and direct construction") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)) {
                                continue;
                            }
                            String methodName = method.getName();
                            if ("toDto".equals(methodName)
                                    || "toResponse".equals(methodName)
                                    || "fromDto".equals(methodName)
                                    || "fromResponse".equals(methodName)) {
                                events.add(
                                        SimpleConditionEvent.violated(
                                                method,
                                                method.getFullName()
                                                        + " declares local dto/response mapping; move mapping to "
                                                        + "application.assembler"));
                            }
                        }
                        for (JavaConstructorCall constructorCall : item.getConstructorCallsFromSelf()) {
                            JavaClass targetOwner = constructorCall.getTargetOwner();
                            String targetPackage = targetOwner.getPackageName();
                            boolean isBusinessModel = targetPackage.startsWith(ROOT_PACKAGE + ".");
                            boolean isDtoOrResponse = targetOwner.getSimpleName().endsWith("DTO")
                                    || targetOwner.getSimpleName().endsWith("Response");
                            if (isBusinessModel && isDtoOrResponse) {
                                events.add(SimpleConditionEvent.violated(
                                        item,
                                        constructorCall.getDescription()
                                                + " constructs business DTO/Response directly; "
                                                + "use application.assembler instead"));
                            }
                        }
                    }
                })
                .because(
                        "RULE LAYER_APPLICATION_ASSEMBLER_EXCLUSIVE_MAPPING: application.command/query/audit/support "
                                + "must map DTO/Response in application.assembler");
    }

    public static ArchRule controllerPublicMethodsShouldUseRequestAndResponse(String basePackage) {
        return interfaceEndpointPublicMethodsShouldUseRequestAndResponse(
                basePackage,
                basePackage + ".interfaces.controller..",
                "controller",
                "RULE LAYER_CONTROLLER_SIGNATURE_REQUEST_RESPONSE: interfaces.controller public methods must use "
                        + "request/response contracts");
    }

    public static ArchRule providerPublicMethodsShouldUseRequestAndResponse(String basePackage) {
        return interfaceEndpointPublicMethodsShouldUseRequestAndResponse(
                basePackage,
                basePackage + ".interfaces.provider..",
                "provider",
                "RULE LAYER_PROVIDER_SIGNATURE_REQUEST_RESPONSE: interfaces.provider public methods must use "
                        + "request/response contracts");
    }

    public static ArchRule interfacesAssemblersShouldOnlyBeCalledByInterfaces(String basePackage) {
        return noClassesOutsidePackageShouldCallClassesInPackage(
                basePackage + ".interfaces..",
                basePackage + ".interfaces.assembler..",
                "interfaces.*InterfaceAssembler",
                "RULE NAME_INTERFACE_ASSEMBLER_CALL_BOUNDARY: interfaces.*InterfaceAssembler 只能被 interfaces 调用");
    }

    public static ArchRule applicationAssemblersShouldOnlyBeCalledByApplication(String basePackage) {
        return noClassesOutsidePackageShouldCallClassesInPackage(
                basePackage + ".application..",
                basePackage + ".application.assembler..",
                "application.*Assembler",
                "RULE NAME_APPLICATION_ASSEMBLER_CALL_BOUNDARY: application.*Assembler 只能被 application 调用");
    }

    public static ArchRule persistenceAssemblersShouldOnlyBeCalledByInfra(String basePackage) {
        return noClassesOutsidePackageShouldCallClassesInPackage(
                basePackage + ".infra..",
                basePackage + ".infra.persistence.assembler..",
                "infra.*PersistenceAssembler",
                "RULE NAME_PERSISTENCE_ASSEMBLER_CALL_BOUNDARY: infra.*PersistenceAssembler 只能被 infra 调用");
    }

    public static ArchRule applicationAndInfraRepositoryShouldNotUseIllegalArgumentException(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage(
                        basePackage + ".application..",
                        basePackage + ".infra.repository.impl..",
                        basePackage + ".infra.repository.support..")
                .should(new ArchCondition<>("construct IllegalArgumentException as business exception") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaConstructorCall constructorCall : item.getConstructorCallsFromSelf()) {
                            JavaClass targetOwner = constructorCall.getTargetOwner();
                            if (IllegalArgumentException.class.getName().equals(targetOwner.getName())) {
                                events.add(SimpleConditionEvent.violated(item, constructorCall.getDescription()));
                            }
                        }
                    }
                })
                .because(
                        "RULE LAYER_APPLICATION_INFRA_NO_ILLEGAL_ARGUMENT: application and "
                                + "infra.repository.impl/support must not use IllegalArgumentException "
                                + "as business exception");
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
                        for (JavaField field : item.getFields()) {
                            if (hasAnyAnnotation(field.getAnnotations(), annotationNames)) {
                                events.add(SimpleConditionEvent.violated(
                                        field, field.getFullName() + " is annotated with " + annotationDescription));
                            }
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

    private static ArchRule noClassesOutsidePackageShouldUseAnnotations(
            String allowedPackageA,
            String allowedPackageB,
            List<String> annotationNames,
            String annotationDescription,
            String because) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideOutsideOfPackages(allowedPackageA, allowedPackageB)
                .should(new ArchCondition<>("use " + annotationDescription) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (hasAnyAnnotation(item.getAnnotations(), annotationNames)) {
                            events.add(SimpleConditionEvent.violated(
                                    item, item.getName() + " is annotated with " + annotationDescription));
                        }
                        for (JavaField field : item.getFields()) {
                            if (hasAnyAnnotation(field.getAnnotations(), annotationNames)) {
                                events.add(SimpleConditionEvent.violated(
                                        field, field.getFullName() + " is annotated with " + annotationDescription));
                            }
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

    private static ArchRule noClassesOutsidePackageShouldCallClassesInPackage(
            String allowedCallerPackage, String calleePackage, String subject, String because) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideOutsideOfPackage(allowedCallerPackage)
                .should(new ArchCondition<>("call " + subject) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethodCall methodCall : item.getMethodCallsFromSelf()) {
                            String targetPackage = methodCall.getTargetOwner().getPackageName();
                            if (targetPackage.startsWith(calleePackage)) {
                                events.add(SimpleConditionEvent.violated(item, methodCall.getDescription()));
                            }
                        }
                        for (JavaConstructorCall constructorCall : item.getConstructorCallsFromSelf()) {
                            String targetPackage = constructorCall.getTargetOwner().getPackageName();
                            if (targetPackage.startsWith(calleePackage)) {
                                events.add(SimpleConditionEvent.violated(item, constructorCall.getDescription()));
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

    private static ArchRule interfaceEndpointPublicMethodsShouldUseRequestAndResponse(
            String basePackage, String endpointPackage, String endpointType, String because) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(endpointPackage)
                .should(new ArchCondition<>("use stable request/response contracts on public " + endpointType + " methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)
                                    || !method.getModifiers().contains(JavaModifier.PUBLIC)) {
                                continue;
                            }
                            for (JavaClass parameterType : method.getRawParameterTypes()) {
                                if (!isAllowedInterfaceEndpointParameterType(basePackage, parameterType)) {
                                    events.add(SimpleConditionEvent.violated(
                                            method,
                                            method.getFullName() + " uses forbidden parameter type "
                                                    + parameterType.getFullName()));
                                }
                            }
                            if (!isAllowedInterfaceEndpointReturnType(basePackage, method)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        method.getFullName() + " uses forbidden return type "
                                                + method.getRawReturnType().getFullName()));
                            }
                        }
                    }
                })
                .because(because);
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

    private static boolean isAllowedInterfaceEndpointParameterType(String basePackage, JavaClass parameterType) {
        String packageName = parameterType.getPackageName();
        if (!packageName.startsWith(ROOT_PACKAGE + ".")) {
            return true;
        }
        return packageName.startsWith(basePackage + ".interfaces.request")
                && parameterType.getSimpleName().endsWith("Request");
    }

    private static boolean isAllowedInterfaceEndpointReturnType(String basePackage, JavaMethod method) {
        return isAllowedInterfaceEndpointReturnType(basePackage, method.reflect().getGenericReturnType());
    }

    private static boolean isAllowedInterfaceEndpointReturnType(String basePackage, Type returnType) {
        if (returnType instanceof Class<?> rawClass) {
            return isAllowedInterfaceEndpointReturnClass(basePackage, rawClass);
        }
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class<?> rawClass)) {
                return false;
            }
            if (isAllowedCollectionWrapper(rawClass) || isResponseEntity(rawClass)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                return actualTypeArguments.length == 1
                        && isAllowedInterfaceEndpointResponsePayload(basePackage, actualTypeArguments[0]);
            }
            return isAllowedInterfaceEndpointReturnClass(basePackage, rawClass);
        }
        return false;
    }

    private static boolean isAllowedInterfaceEndpointResponsePayload(String basePackage, Type payloadType) {
        if (payloadType instanceof Class<?> payloadClass) {
            return isAllowedInterfaceEndpointReturnClass(basePackage, payloadClass);
        }
        if (payloadType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class<?> rawClass) || !isAllowedCollectionWrapper(rawClass)) {
                return false;
            }
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return actualTypeArguments.length == 1
                    && isAllowedInterfaceEndpointResponsePayload(basePackage, actualTypeArguments[0]);
        }
        return false;
    }

    private static boolean isAllowedInterfaceEndpointReturnClass(String basePackage, Class<?> returnClass) {
        String packageName = returnClass.getPackageName();
        if (void.class.equals(returnClass) || Void.class.equals(returnClass)) {
            return true;
        }
        if (!packageName.startsWith(ROOT_PACKAGE + ".")) {
            return isAllowedCollectionWrapper(returnClass) || isResponseEntity(returnClass);
        }
        return packageName.startsWith(basePackage + ".interfaces.response")
                && returnClass.getSimpleName().endsWith("Response");
    }

    private static boolean isAllowedCollectionWrapper(Class<?> rawClass) {
        return List.class.equals(rawClass) || Set.class.equals(rawClass);
    }

    private static boolean isResponseEntity(Class<?> rawClass) {
        return "org.springframework.http.ResponseEntity".equals(rawClass.getName());
    }

    private static boolean isWhitelistedInterfacesDependency(
            String basePackage, String sourceDomain, JavaClass sourceClass, String targetPackage) {
        if (targetPackage.startsWith("com.github.thundax.bacon.common.")) {
            return true;
        }
        if (targetPackage.startsWith(basePackage + ".interfaces.")) {
            return true;
        }
        if (targetPackage.startsWith(basePackage + ".application.")) {
            return true;
        }
        if (targetPackage.startsWith(basePackage + ".domain.model.")) {
            return true;
        }
        if (targetPackage.startsWith(basePackage + ".api.facade.")) {
            return true;
        }
        if (sourceClass.getPackageName().startsWith(basePackage + ".interfaces.facade.")
                && targetPackage.startsWith(basePackage + ".api.")) {
            return true;
        }
        String targetDomain = extractDomainSegment(targetPackage);
        return !sourceDomain.isEmpty()
                && !targetDomain.isEmpty()
                && !targetDomain.equals(sourceDomain)
                && targetPackage.startsWith(ROOT_PACKAGE + "." + targetDomain + ".api.facade.");
    }

    private static boolean isOtherDomainBusinessModulePackage(String sourceDomain, String targetPackage) {
        if (!targetPackage.startsWith(ROOT_PACKAGE + ".")) {
            return false;
        }
        String relativePackage = targetPackage.substring((ROOT_PACKAGE + ".").length());
        String[] segments = relativePackage.split("\\.");
        if (segments.length < 2) {
            return false;
        }
        for (int i = 1; i < segments.length; i++) {
            String targetLayer = segments[i];
            String targetDomain = segments[i - 1];
            if (BUSINESS_LAYERS.contains(targetLayer) && BUSINESS_DOMAINS.contains(targetDomain)) {
                return !targetDomain.equals(sourceDomain);
            }
        }
        return false;
    }

    private static String extractDomainSegment(String basePackage) {
        String[] segments = basePackage.split("\\.");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (BUSINESS_DOMAINS.contains(segments[i])) {
                return segments[i];
            }
        }
        return "";
    }
}
