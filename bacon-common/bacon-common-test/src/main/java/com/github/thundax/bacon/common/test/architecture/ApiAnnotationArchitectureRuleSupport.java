package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.util.List;
import java.util.Set;

public final class ApiAnnotationArchitectureRuleSupport {

    private static final String INTERFACES_CONTROLLER_PACKAGE_SEGMENT = ".interfaces.controller";
    private static final String INTERFACES_PROVIDER_PACKAGE_SEGMENT = ".interfaces.provider";

    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";
    private static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String GET_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.GetMapping";
    private static final String POST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PostMapping";
    private static final String PUT_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PutMapping";
    private static final String DELETE_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.DeleteMapping";
    private static final String PATCH_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PatchMapping";
    private static final String VALIDATED_ANNOTATION = "org.springframework.validation.annotation.Validated";
    private static final String TAG_ANNOTATION = "io.swagger.v3.oas.annotations.tags.Tag";
    private static final String OPERATION_ANNOTATION = "io.swagger.v3.oas.annotations.Operation";
    private static final String HAS_PERMISSION_ANNOTATION = "com.github.thundax.bacon.common.security.annotation.HasPermission";
    private static final String SYS_LOG_ANNOTATION = "com.github.thundax.bacon.common.log.annotation.SysLog";
    private static final String WRAPPED_API_CONTROLLER_ANNOTATION =
            "com.github.thundax.bacon.common.web.annotation.WrappedApiController";
    private static final String API_ANNOTATION_EXCEPTION_ANNOTATION =
            "com.github.thundax.bacon.common.web.annotation.ApiAnnotationException";

    private static final Set<String> MAPPING_ANNOTATIONS = Set.of(
            GET_MAPPING_ANNOTATION,
            POST_MAPPING_ANNOTATION,
            PUT_MAPPING_ANNOTATION,
            DELETE_MAPPING_ANNOTATION,
            PATCH_MAPPING_ANNOTATION);

    private static final Set<String> ENDPOINT_ANNOTATIONS = Set.of(
            REST_CONTROLLER_ANNOTATION,
            REQUEST_MAPPING_ANNOTATION,
            GET_MAPPING_ANNOTATION,
            POST_MAPPING_ANNOTATION,
            PUT_MAPPING_ANNOTATION,
            DELETE_MAPPING_ANNOTATION,
            PATCH_MAPPING_ANNOTATION);

    private static final Set<String> ALLOWED_EXCEPTION_BUCKETS =
            Set.of("AUTH_PUBLIC", "OAUTH2_PROTOCOL", "CALLBACK_ENDPOINT");

    private ApiAnnotationArchitectureRuleSupport() {}

    public static JavaClasses importDomainClasses(String basePackage) {
        return NamingAndPlacementRuleSupport.importDomainClasses(basePackage);
    }

    public static ArchRule commonClassBaseAnnotationsRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAnyPackage(
                        basePackage + ".interfaces.controller..",
                        basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare class base annotations for API endpoints") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        List<String> missing = List.of(
                                        REST_CONTROLLER_ANNOTATION,
                                        REQUEST_MAPPING_ANNOTATION,
                                        VALIDATED_ANNOTATION,
                                        TAG_ANNOTATION)
                                .stream()
                                .filter(annotationName -> !hasAnnotation(item, annotationName))
                                .toList();
                        if (!missing.isEmpty()) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "[ANNO_COMMON_CLASS_BASE_REQUIRED] " + item.getFullName()
                                            + " violates class base annotations required: " + missing));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_COMMON_CLASS_BASE_REQUIRED");
    }

    public static ArchRule commonHttpMappingRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAnyPackage(
                        basePackage + ".interfaces.controller..",
                        basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare exactly one mapping annotation on each endpoint method") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            int mappingCount = mappingAnnotationCount(method);
                            if (mappingCount != 1) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_COMMON_METHOD_MAPPING_REQUIRED] " + formatMethod(item, method)
                                                + " violates method mapping required: " + mappingCount));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_COMMON_METHOD_MAPPING_REQUIRED");
    }

    public static ArchRule bffWrappedApiControllerRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .and(new com.tngtech.archunit.base.DescribedPredicate<>("not callback class") {
                    @Override
                    public boolean test(JavaClass input) {
                        return !isCallbackClass(input);
                    }
                })
                .should(new ArchCondition<>("declare @WrappedApiController on BFF controller class") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!hasAnnotation(item, WRAPPED_API_CONTROLLER_ANNOTATION)) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "[ANNO_BFF_CLASS_WRAPPED_REQUIRED] " + item.getFullName()
                                            + " violates WrappedApiController required: []"));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_BFF_CLASS_WRAPPED_REQUIRED");
    }

    public static ArchRule bffOperationRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare @Operation on BFF endpoint methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (mappingAnnotationCount(method) == 0 || isCallbackMethod(item, method)) {
                                continue;
                            }
                            if (!hasAnnotation(method, OPERATION_ANNOTATION)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_BFF_METHOD_OPERATION_REQUIRED] " + formatMethod(item, method)
                                                + " violates Operation required: []"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_BFF_METHOD_OPERATION_REQUIRED");
    }

    public static ArchRule bffPermissionRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare @HasPermission on BFF endpoint methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (mappingAnnotationCount(method) == 0 || isCallbackMethod(item, method)) {
                                continue;
                            }
                            if (hasAllowedApiAnnotationException(item, method)) {
                                continue;
                            }
                            if (!hasAnnotation(method, HAS_PERMISSION_ANNOTATION)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_BFF_PERMISSION_REQUIRED] " + formatMethod(item, method)
                                                + " violates HasPermission required: []"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_BFF_PERMISSION_REQUIRED");
    }

    public static ArchRule callbackOperationRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare @Operation on callback endpoint methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (!isCallbackMethod(item, method)) {
                                continue;
                            }
                            if (!hasAnnotation(method, OPERATION_ANNOTATION)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_CALLBACK_OPERATION_REQUIRED] " + formatMethod(item, method)
                                                + " violates Operation required: []"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_CALLBACK_OPERATION_REQUIRED");
    }

    public static ArchRule callbackPermissionForbidden(String basePackage) {
        return callbackMethodForbiddenAnnotationRule(
                basePackage,
                HAS_PERMISSION_ANNOTATION,
                "ANNO_CALLBACK_PERMISSION_FORBIDDEN",
                "HasPermission");
    }

    public static ArchRule callbackSysLogForbidden(String basePackage) {
        return callbackMethodForbiddenAnnotationRule(
                basePackage,
                SYS_LOG_ANNOTATION,
                "ANNO_CALLBACK_SYSLOG_FORBIDDEN",
                "SysLog");
    }

    public static ArchRule providerPathPrefixRequired(String basePackage) {
        return NamingAndPlacementRuleSupport.providerControllerRequestMappingShouldUseDomainPrefix(basePackage)
                .because("RULE ANNO_PROVIDER_PATH_PREFIX_REQUIRED");
    }

    public static ArchRule providerOperationRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("declare @Operation on provider endpoint methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (mappingAnnotationCount(method) == 0) {
                                continue;
                            }
                            if (!hasAnnotation(method, OPERATION_ANNOTATION)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_PROVIDER_METHOD_OPERATION_REQUIRED] " + formatMethod(item, method)
                                                + " violates Operation required: []"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_PROVIDER_METHOD_OPERATION_REQUIRED");
    }

    public static ArchRule providerPermissionForbidden(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("forbid @HasPermission on provider endpoint methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (mappingAnnotationCount(method) == 0) {
                                continue;
                            }
                            if (hasAnnotation(method, HAS_PERMISSION_ANNOTATION)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_PROVIDER_PERMISSION_FORBIDDEN] " + formatMethod(item, method)
                                                + " violates HasPermission forbidden: [HasPermission]"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_PROVIDER_PERMISSION_FORBIDDEN");
    }

    public static ArchRule facadeEndpointAnnotationsForbidden(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.facade..")
                .should(new ArchCondition<>("forbid endpoint annotations on facade classes and methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (String annotationName : ENDPOINT_ANNOTATIONS) {
                            if (hasAnnotation(item, annotationName)) {
                                events.add(SimpleConditionEvent.violated(
                                        item,
                                        "[ANNO_FACADE_ENDPOINT_ANNOTATION_FORBIDDEN] " + item.getFullName()
                                                + " violates endpoint annotation forbidden: [" + shortName(annotationName) + "]"));
                            }
                        }
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)) {
                                continue;
                            }
                            for (String annotationName : ENDPOINT_ANNOTATIONS) {
                                if (hasAnnotation(method, annotationName)) {
                                    events.add(SimpleConditionEvent.violated(
                                            method,
                                            "[ANNO_FACADE_ENDPOINT_ANNOTATION_FORBIDDEN] "
                                                    + formatMethod(item, method)
                                                    + " violates endpoint annotation forbidden: ["
                                                    + shortName(annotationName) + "]"));
                                }
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_FACADE_ENDPOINT_ANNOTATION_FORBIDDEN");
    }

    public static ArchRule facadeSecurityAnnotationsForbidden(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.facade..")
                .should(new ArchCondition<>("forbid security annotations on facade classes and methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (String annotationName : List.of(HAS_PERMISSION_ANNOTATION, SYS_LOG_ANNOTATION)) {
                            if (hasAnnotation(item, annotationName)) {
                                events.add(SimpleConditionEvent.violated(
                                        item,
                                        "[ANNO_FACADE_SECURITY_ANNOTATION_FORBIDDEN] " + item.getFullName()
                                                + " violates security annotation forbidden: [" + shortName(annotationName)
                                                + "]"));
                            }
                        }
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)) {
                                continue;
                            }
                            for (String annotationName : List.of(HAS_PERMISSION_ANNOTATION, SYS_LOG_ANNOTATION)) {
                                if (hasAnnotation(method, annotationName)) {
                                    events.add(SimpleConditionEvent.violated(
                                            method,
                                            "[ANNO_FACADE_SECURITY_ANNOTATION_FORBIDDEN] "
                                                    + formatMethod(item, method)
                                                    + " violates security annotation forbidden: ["
                                                    + shortName(annotationName) + "]"));
                                }
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_FACADE_SECURITY_ANNOTATION_FORBIDDEN");
    }

    public static ArchRule exceptionAnnotationScopeRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + "..")
                .should(new ArchCondition<>("limit ApiAnnotationException to interfaces.controller/provider") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        boolean allowedPackage = isControllerOrProviderPackage(item.getPackageName());
                        if (hasAnnotation(item, API_ANNOTATION_EXCEPTION_ANNOTATION) && !allowedPackage) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "[ANNO_EXCEPTION_ANNOTATION_SCOPE_REQUIRED] " + item.getFullName()
                                            + " violates ApiAnnotationException scope required: "
                                            + item.getPackageName()));
                        }
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)) {
                                continue;
                            }
                            if (hasAnnotation(method, API_ANNOTATION_EXCEPTION_ANNOTATION) && !allowedPackage) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_EXCEPTION_ANNOTATION_SCOPE_REQUIRED] "
                                                + formatMethod(item, method)
                                                + " violates ApiAnnotationException scope required: "
                                                + item.getPackageName()));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_EXCEPTION_ANNOTATION_SCOPE_REQUIRED");
    }

    public static ArchRule exceptionAnnotationBucketEnumRequired(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + "..")
                .should(new ArchCondition<>("enforce ApiAnnotationException bucket enum values") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        checkBucketValue(item, item.getAnnotations(), events, item.getFullName());
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)) {
                                continue;
                            }
                            checkBucketValue(
                                    item,
                                    method.getAnnotations(),
                                    events,
                                    formatMethod(item, method));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_EXCEPTION_ANNOTATION_BUCKET_ENUM_REQUIRED");
    }

    public static ArchRule exceptionAnnotationAppliesPermissionRulesOnly(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAnyPackage(
                        basePackage + ".interfaces.controller..",
                        basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("allow ApiAnnotationException to bypass permission rule only") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        boolean classAnnotated = hasAnnotation(item, API_ANNOTATION_EXCEPTION_ANNOTATION);
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            boolean methodAnnotated = hasAnnotation(method, API_ANNOTATION_EXCEPTION_ANNOTATION);
                            if (!classAnnotated && !methodAnnotated) {
                                continue;
                            }
                            int mappingCount = mappingAnnotationCount(method);
                            boolean hasOperation = hasAnnotation(method, OPERATION_ANNOTATION);
                            if (mappingCount != 1 || !hasOperation) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[ANNO_EXCEPTION_ANNOTATION_APPLIES_RULES_ONLY] "
                                                + formatMethod(item, method)
                                                + " violates ApiAnnotationException applies-rules-only constraint: "
                                                + "mappingCount=" + mappingCount + ", hasOperation=" + hasOperation));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE ANNO_EXCEPTION_ANNOTATION_APPLIES_RULES_ONLY");
    }

    private static ArchRule callbackMethodForbiddenAnnotationRule(
            String basePackage, String forbiddenAnnotation, String ruleId, String annotationLabel) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("forbid callback method annotation " + annotationLabel) {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : declaredPublicMethods(item)) {
                            if (!isCallbackMethod(item, method)) {
                                continue;
                            }
                            if (hasAnnotation(method, forbiddenAnnotation)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        "[" + ruleId + "] " + formatMethod(item, method)
                                                + " violates " + annotationLabel + " forbidden: ["
                                                + annotationLabel + "]"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE " + ruleId);
    }

    private static List<JavaMethod> declaredPublicMethods(JavaClass item) {
        return item.getMethods().stream()
                .filter(method -> method.getOwner().equals(item))
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                .toList();
    }

    private static boolean isControllerOrProviderPackage(String packageName) {
        return packageName.contains(INTERFACES_CONTROLLER_PACKAGE_SEGMENT)
                || packageName.contains(INTERFACES_PROVIDER_PACKAGE_SEGMENT);
    }

    private static boolean isCallbackClass(JavaClass item) {
        if (item.getSimpleName().endsWith("CallbackController")) {
            return true;
        }
        return hasMappingAnnotationWithKeyword(item.getAnnotations(), "callback");
    }

    private static boolean isCallbackMethod(JavaClass owner, JavaMethod method) {
        if (isCallbackClass(owner)) {
            return true;
        }
        return hasMappingAnnotationWithKeyword(method.getAnnotations(), "callback");
    }

    private static boolean hasAllowedApiAnnotationException(JavaClass item, JavaMethod method) {
        return hasAllowedExceptionBucket(method.getAnnotations()) || hasAllowedExceptionBucket(item.getAnnotations());
    }

    private static boolean hasAllowedExceptionBucket(Iterable<? extends JavaAnnotation<?>> annotations) {
        for (JavaAnnotation<?> annotation : annotations) {
            if (!API_ANNOTATION_EXCEPTION_ANNOTATION.equals(annotation.getRawType().getName())) {
                continue;
            }
            String bucket = resolveBucketName(annotation);
            if (ALLOWED_EXCEPTION_BUCKETS.contains(bucket)) {
                return true;
            }
        }
        return false;
    }

    private static void checkBucketValue(
            JavaClass owner,
            Iterable<? extends JavaAnnotation<?>> annotations,
            ConditionEvents events,
            String scope) {
        for (JavaAnnotation<?> annotation : annotations) {
            if (!API_ANNOTATION_EXCEPTION_ANNOTATION.equals(annotation.getRawType().getName())) {
                continue;
            }
            String bucket = resolveBucketName(annotation);
            boolean matched = ALLOWED_EXCEPTION_BUCKETS.contains(bucket);
            if (!matched) {
                events.add(SimpleConditionEvent.violated(
                        owner,
                        "[ANNO_EXCEPTION_ANNOTATION_BUCKET_ENUM_REQUIRED] " + scope
                                + " violates ApiAnnotationException bucket enum required: "
                                + bucket));
            }
        }
    }

    private static String resolveBucketName(JavaAnnotation<?> annotation) {
        return annotation.get("bucket")
                .map(value -> {
                    if (value instanceof JavaEnumConstant javaEnumConstant) {
                        return javaEnumConstant.name();
                    }
                    if (value instanceof Enum<?> enumValue) {
                        return enumValue.name();
                    }
                    return String.valueOf(value);
                })
                .orElse("");
    }

    private static int mappingAnnotationCount(JavaMethod method) {
        int count = 0;
        for (JavaAnnotation<?> annotation : method.getAnnotations()) {
            if (MAPPING_ANNOTATIONS.contains(annotation.getRawType().getName())) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasMappingAnnotationWithKeyword(
            Iterable<? extends JavaAnnotation<?>> annotations, String keyword) {
        for (JavaAnnotation<?> annotation : annotations) {
            String annotationName = annotation.getRawType().getName();
            if (!REQUEST_MAPPING_ANNOTATION.equals(annotationName) && !MAPPING_ANNOTATIONS.contains(annotationName)) {
                continue;
            }
            if (annotation.toString().toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnnotation(JavaClass item, String annotationName) {
        return hasAnnotation(item.getAnnotations(), annotationName);
    }

    private static boolean hasAnnotation(JavaMethod method, String annotationName) {
        return hasAnnotation(method.getAnnotations(), annotationName);
    }

    private static boolean hasAnnotation(
            Iterable<? extends JavaAnnotation<?>> annotations, String annotationName) {
        for (JavaAnnotation<?> annotation : annotations) {
            if (annotationName.equals(annotation.getRawType().getName())) {
                return true;
            }
        }
        return false;
    }

    private static String shortName(String fullName) {
        int index = fullName.lastIndexOf('.');
        return index >= 0 ? fullName.substring(index + 1) : fullName;
    }

    private static String formatMethod(JavaClass owner, JavaMethod method) {
        return owner.getFullName() + "#" + method.getName();
    }
}
