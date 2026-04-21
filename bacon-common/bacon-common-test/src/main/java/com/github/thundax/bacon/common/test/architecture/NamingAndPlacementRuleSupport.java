package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NamingAndPlacementRuleSupport {

    private static final Set<String> REPOSITORY_METHOD_PREFIX_WHITELIST = Set.of(
            "find",
            "list",
            "page",
            "count",
            "exists",
            "claim",
            "release",
            "renew",
            "mark",
            "pause",
            "resume",
            "recover",
            "delete",
            "insert",
            "update");

    private NamingAndPlacementRuleSupport() {}

    public static JavaClasses importDomainClasses(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);
    }

    public static ArchRule controllerShouldUseControllerNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Controller")
                .and()
                .haveNameNotMatching(".*ProviderController$")
                .should()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .allowEmptyShould(true)
                .because("Controller -> interfaces.controller");
    }

    public static ArchRule providerControllerShouldUseProviderControllerNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("ProviderController")
                .should()
                .resideInAPackage(basePackage + ".interfaces.provider..")
                .allowEmptyShould(true)
                .because("ProviderController -> interfaces.provider");
    }

    public static ArchRule resolverShouldUseResolverNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Resolver")
                .should()
                .resideInAPackage(basePackage + ".interfaces.resolver..")
                .allowEmptyShould(true)
                .because("Resolver -> interfaces.resolver");
    }

    public static ArchRule applicationServiceShouldUseApplicationServiceNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("ApplicationService")
                .should()
                .resideInAnyPackage(
                        basePackage + ".application.command..",
                        basePackage + ".application.query..",
                        basePackage + ".application.audit..",
                        basePackage + ".application.support..")
                .allowEmptyShould(true)
                .because(
                        "ApplicationService -> application.command/query/audit, temporarily allows application.support");
    }

    public static ArchRule domainServiceShouldUseDomainServiceNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("DomainService")
                .should()
                .resideInAPackage(basePackage + ".domain.service..")
                .allowEmptyShould(true)
                .because("DomainService -> domain.service");
    }

    public static ArchRule repositoryShouldUseRepositoryNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Repository")
                .should()
                .resideInAPackage(basePackage + ".domain.repository..")
                .allowEmptyShould(true)
                .because("Repository -> domain.repository");
    }

    public static ArchRule repositoryImplShouldUseRepositoryImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("RepositoryImpl")
                .should()
                .resideInAPackage(basePackage + ".infra.repository.impl..")
                .allowEmptyShould(true)
                .because("RepositoryImpl -> infra.repository.impl");
    }

    public static ArchRule repositoryImplShouldNotDependOnOtherRepositoryImpl(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("RepositoryImpl")
                .and()
                .resideInAPackage(basePackage + ".infra.repository.impl..")
                .should(new ArchCondition<>("avoid direct dependency on other RepositoryImpl") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        List<String> referencedTypes = findSimpleTypeReferences(item, "RepositoryImpl");
                        referencedTypes.stream()
                                .filter(typeName -> !typeName.equals(item.getSimpleName()))
                                .forEach(typeName -> events.add(SimpleConditionEvent.violated(
                                        item,
                                        item.getFullName() + " references " + typeName
                                                + " violates RULE NAME_REPOSITORY_IMPL_NO_CROSS_IMPL_DEP")));
                    }
                })
                .allowEmptyShould(true)
                .because(
                        "RULE NAME_REPOSITORY_IMPL_NO_CROSS_IMPL_DEP: RepositoryImpl must not depend on other RepositoryImpl");
    }

    public static ArchRule repositoryImplShouldOnlyDependOnOwnPersistenceSupport(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("RepositoryImpl")
                .and()
                .resideInAPackage(basePackage + ".infra.repository.impl..")
                .should(new ArchCondition<>("avoid dependency on other aggregate PersistenceSupport") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        String aggregatePrefix = aggregatePrefix(item.getSimpleName());
                        findSimpleTypeReferences(item, "PersistenceSupport").stream()
                                .filter(typeName -> !typeName.startsWith(aggregatePrefix))
                                .forEach(typeName -> events.add(SimpleConditionEvent.violated(
                                        item,
                                        item.getFullName() + " references " + typeName
                                                + " violates RULE NAME_REPOSITORY_IMPL_NO_CROSS_SUPPORT_DEP; allowed aggregate prefix is "
                                                + aggregatePrefix)));
                    }
                })
                .allowEmptyShould(true)
                .because(
                        "RULE NAME_REPOSITORY_IMPL_NO_CROSS_SUPPORT_DEP: RepositoryImpl must not depend on other aggregate PersistenceSupport");
    }

    public static ArchRule repositoryImplShouldImplementSingleDomainRepository(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("RepositoryImpl")
                .and()
                .resideInAPackage(basePackage + ".infra.repository.impl..")
                .should(new ArchCondition<>("implement one and only one domain repository interface") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        Set<JavaClass> interfaces = item.getRawInterfaces();
                        if (interfaces.size() != 1) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    item.getFullName() + " implements " + interfaces.stream()
                                                    .map(JavaClass::getFullName)
                                                    .sorted()
                                                    .toList()
                                            + " violation: RepositoryImpl must implement exactly one interface"));
                            return;
                        }
                        JavaClass implementedInterface = interfaces.iterator().next();
                        boolean isDomainRepositoryInterface = implementedInterface
                                        .getPackageName()
                                        .startsWith(basePackage + ".domain.repository")
                                && implementedInterface.getSimpleName().endsWith("Repository");
                        if (!isDomainRepositoryInterface) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    item.getFullName() + " implements " + implementedInterface.getFullName()
                                            + " violation: interface must be in " + basePackage
                                            + ".domain.repository.. and end with Repository"));
                            return;
                        }
                        events.add(SimpleConditionEvent.satisfied(
                                item,
                                item.getFullName() + " implements " + implementedInterface.getFullName()
                                        + " and satisfies single-domain-repository rule"));
                    }
                })
                .allowEmptyShould(true)
                .because(
                        "RULE NAME_REPOSITORY_IMPL_SINGLE_DOMAIN_REPOSITORY: "
                                + "RepositoryImpl must implement one and only one "
                                + "domain.repository Repository");
    }

    public static ArchRule repositoryMethodShouldUseWhitelistedPrefix(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Repository")
                .and()
                .resideInAPackage(basePackage + ".domain.repository..")
                .should(new ArchCondition<>("declare repository methods using whitelisted prefixes only") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        item.getMethods().stream()
                                .filter(method -> method.getOwner().equals(item))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.SYNTHETIC))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.BRIDGE))
                                .forEach(method -> {
                                    String methodName = method.getName();
                                    boolean matched = REPOSITORY_METHOD_PREFIX_WHITELIST.stream()
                                            .anyMatch(methodName::startsWith);
                                    String detail = matched
                                            ? formatMethod(item, method) + " uses whitelisted repository prefix"
                                            : formatMethod(item, method)
                                                    + " violation: repository method prefix must be one of "
                                                    + REPOSITORY_METHOD_PREFIX_WHITELIST;
                                    events.add(new SimpleConditionEvent(method, matched, detail));
                                });
                    }
                })
                .allowEmptyShould(true)
                .because(
                        "RULE NAME_REPOSITORY_METHOD_PREFIX: Repository method prefixes must use the whitelist only");
    }

    public static ArchRule mapperShouldUseMapperNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Mapper")
                .should()
                .resideInAPackage(basePackage + ".infra.persistence.mapper..")
                .allowEmptyShould(true)
                .because("Mapper -> infra.persistence.mapper");
    }

    public static ArchRule dataObjectShouldUseDONameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("DO")
                .should()
                .resideInAPackage(basePackage + ".infra.persistence.dataobject..")
                .allowEmptyShould(true)
                .because("DO -> infra.persistence.dataobject");
    }

    public static ArchRule shouldNotUseDataObjectSuffix(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + "..")
                .should()
                .haveSimpleNameEndingWith("DataObject")
                .because("Persistence objects must use the DO suffix; do not use DataObject.");
    }

    public static ArchRule converterShouldUseConverterNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Converter")
                .should()
                .resideInAPackage(basePackage + ".infra.repository.converter..")
                .allowEmptyShould(true)
                .because("Converter -> infra.repository.converter");
    }

    public static ArchRule codecShouldUseCodecNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Codec")
                .should()
                .resideInAPackage(basePackage + ".application.codec..")
                .allowEmptyShould(true)
                .because("Codec -> application.codec");
    }

    public static ArchRule persistenceAssemblerShouldUsePersistenceAssemblerNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("PersistenceAssembler")
                .should()
                .resideInAPackage(basePackage + ".infra.persistence.assembler..")
                .allowEmptyShould(true)
                .because("RULE LAYER_PERSISTENCE_ASSEMBLER_PLACEMENT: PersistenceAssembler must reside in infra.persistence.assembler");
    }

    public static ArchRule persistenceAssemblerPublicMethodsShouldUseToDomainAndToDataObject(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("PersistenceAssembler")
                .and()
                .resideInAPackage(basePackage + ".infra.persistence.assembler..")
                .should(new ArchCondition<>("declare only toDomain/toDataObject as public methods") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethod method : item.getMethods()) {
                            if (!method.getOwner().equals(item)
                                    || !method.getModifiers().contains(JavaModifier.PUBLIC)) {
                                continue;
                            }
                            String methodName = method.getName();
                            if (!"toDomain".equals(methodName) && !"toDataObject".equals(methodName)) {
                                events.add(SimpleConditionEvent.violated(
                                        method,
                                        formatMethod(item, method)
                                                + " is public but not allowed in PersistenceAssembler"));
                            }
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE LAYER_PERSISTENCE_ASSEMBLER_PUBLIC_METHODS: PersistenceAssembler public methods must be toDomain/toDataObject only");
    }

    public static ArchRule facadeShouldUseFacadeNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("Facade")
                .should()
                .resideInAPackage(basePackage + ".api.facade..")
                .allowEmptyShould(true)
                .because("Facade -> api.facade");
    }

    public static ArchRule facadeRequestShouldUseFacadeRequestNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.request..")
                .should()
                .haveSimpleNameEndingWith("FacadeRequest")
                .allowEmptyShould(true)
                .because("RULE NAME_FACADE_REQUEST: classes in api.request must end with FacadeRequest");
    }

    public static ArchRule facadeResponseShouldUseFacadeResponseNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.response..")
                .should()
                .haveSimpleNameEndingWith("FacadeResponse")
                .allowEmptyShould(true)
                .because("RULE NAME_FACADE_RESPONSE: classes in api.response must end with FacadeResponse");
    }

    public static ArchRule facadeMethodShouldUseFacadeRequestAndResponse(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.facade..")
                .and()
                .areInterfaces()
                .should(new ArchCondition<>("declare Facade methods using FacadeRequest and FacadeResponse") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        item.getMethods().stream()
                                .filter(method -> method.getOwner().equals(item))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                                .forEach(method -> {
                                    List<String> violations = new ArrayList<>();
                                    boolean requestMatched = method.getRawParameterTypes().isEmpty()
                                            || (method.getRawParameterTypes().size() == 1
                                                    && method.getRawParameterTypes().stream()
                                                            .allMatch(parameterType -> parameterType.getSimpleName()
                                                                            .endsWith("FacadeRequest")
                                                                    && parameterType.getPackageName()
                                                                            .startsWith(basePackage + ".api.request")));
                                    if (!requestMatched) {
                                        violations.add(
                                                "parameters must be empty or use a single "
                                                        + basePackage
                                                        + ".api.request.*FacadeRequest");
                                    }
                                    JavaClass returnType = method.getRawReturnType();
                                    boolean responseMatched = returnType.isEquivalentTo(void.class)
                                            || (returnType.getSimpleName().endsWith("FacadeResponse")
                                                    && returnType.getPackageName().startsWith(basePackage + ".api.response"));
                                    if (!responseMatched) {
                                        violations.add(
                                                "return type must be void or use "
                                                        + basePackage
                                                        + ".api.response.*FacadeResponse");
                                    }

                                    boolean satisfied = violations.isEmpty();
                                    String detail = satisfied
                                            ? formatMethod(item, method) + " uses FacadeRequest/FacadeResponse"
                                            : formatMethod(item, method) + " violation: " + String.join("; ", violations);
                                    events.add(new SimpleConditionEvent(method, satisfied, detail));
                                });
                    }
                })
                .allowEmptyShould(true)
                .because("Facade methods must use zero-or-one FacadeRequest and optional FacadeResponse");
    }

    public static ArchRule facadeMethodShouldUseSingleFacadeRequest(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.facade..")
                .and()
                .areInterfaces()
                .should(new ArchCondition<>("declare Facade methods using a single FacadeRequest parameter") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        item.getMethods().stream()
                                .filter(method -> method.getOwner().equals(item))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                                .forEach(method -> {
                                    boolean requestMatched = method.getRawParameterTypes().isEmpty()
                                            || (method.getRawParameterTypes().size() == 1
                                                    && method.getRawParameterTypes().stream()
                                                            .allMatch(parameterType -> parameterType.getSimpleName()
                                                                            .endsWith("FacadeRequest")
                                                                    && parameterType.getPackageName()
                                                                            .startsWith(basePackage + ".api.request")));
                                    String detail = requestMatched
                                            ? formatMethod(item, method)
                                                    + " uses empty-or-single FacadeRequest parameter"
                                            : formatMethod(item, method)
                                                    + " violation: parameters must be empty or use a single "
                                                    + basePackage
                                                    + ".api.request.*FacadeRequest";
                                    events.add(new SimpleConditionEvent(method, requestMatched, detail));
                                });
                    }
                })
                .allowEmptyShould(true)
                .because("Facade methods must use zero-or-one FacadeRequest parameter");
    }

    public static ArchRule facadeMethodShouldUseFacadeResponse(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".api.facade..")
                .and()
                .areInterfaces()
                .should(new ArchCondition<>("declare Facade methods returning FacadeResponse") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        item.getMethods().stream()
                                .filter(method -> method.getOwner().equals(item))
                                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                                .forEach(method -> {
                                    JavaClass returnType = method.getRawReturnType();
                                    boolean responseMatched = returnType.isEquivalentTo(void.class)
                                            || (returnType.getSimpleName().endsWith("FacadeResponse")
                                                    && returnType.getPackageName().startsWith(basePackage + ".api.response"));
                                    String detail = responseMatched
                                            ? formatMethod(item, method) + " returns FacadeResponse or void"
                                            : formatMethod(item, method)
                                                    + " violation: return type must be void or use "
                                                    + basePackage
                                                    + ".api.response.*FacadeResponse";
                                    events.add(new SimpleConditionEvent(method, responseMatched, detail));
                                });
                    }
                })
                .allowEmptyShould(true)
                .because("Facade methods must return FacadeResponse or void");
    }

    public static ArchRule facadeLocalImplShouldUseFacadeLocalImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("FacadeLocalImpl")
                .should()
                .resideInAPackage(basePackage + ".interfaces.facade..")
                .allowEmptyShould(true)
                .because("FacadeLocalImpl -> interfaces.facade");
    }

    public static ArchRule facadeRemoteImplShouldUseFacadeRemoteImplNameAndPackage(String basePackage) {
        return ArchRuleDefinition.classes()
                .that()
                .haveSimpleNameEndingWith("FacadeRemoteImpl")
                .should()
                .resideInAPackage(basePackage + ".infra.facade.remote..")
                .allowEmptyShould(true)
                .because("FacadeRemoteImpl -> infra.facade.remote");
    }

    public static ArchRule controllerRequestMappingShouldUseDomainPrefix(String basePackage) {
        String domain = domainName(basePackage);
        String prefix = "/" + domain;
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.controller..")
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should(new RequestMappingPathPrefixCondition(prefix, "Controller"))
                .allowEmptyShould(true)
                .because("Controller path must use /{domain}/**");
    }

    public static ArchRule providerControllerRequestMappingShouldUseDomainPrefix(String basePackage) {
        String domain = domainName(basePackage);
        String prefix = "/providers/" + domain;
        return ArchRuleDefinition.classes()
                .that()
                .resideInAPackage(basePackage + ".interfaces.provider..")
                .and()
                .haveSimpleNameEndingWith("ProviderController")
                .should(new RequestMappingPathPrefixCondition(prefix, "ProviderController"))
                .allowEmptyShould(true)
                .because("ProviderController path must use /providers/{domain}/**");
    }

    public static ArchRule simpleEnumShouldUseNameAndFromConvention(String... fullyQualifiedClassNames) {
        Set<String> classNamePatterns = Set.of(fullyQualifiedClassNames);
        return ArchRuleDefinition.classes()
                .that(new DescribedPredicate<>("match configured simple enum classes") {
                    @Override
                    public boolean test(JavaClass input) {
                        return classNamePatterns.stream()
                                .anyMatch(pattern -> matchesClassNamePattern(pattern, input.getFullName()));
                    }
                })
                .and()
                .areEnums()
                .and(new DescribedPredicate<>("are simple enums without instance fields") {
                    @Override
                    public boolean test(JavaClass input) {
                        return input.getFields().stream()
                                .filter(field -> !field.getModifiers().contains(JavaModifier.STATIC))
                                .toList()
                                .isEmpty();
                    }
                })
                .should(
                        new ArchCondition<>(
                                "use value() -> name() and from(String) -> values/equalsIgnoreCase/orElseThrow") {
                            @Override
                            public void check(JavaClass item, ConditionEvents events) {
                                List<String> violations = new ArrayList<>();
                                Optional<JavaMethod> valueMethod = item.tryGetMethod("value");
                                Optional<JavaMethod> fromMethod = item.tryGetMethod("from", String.class);
                                Optional<JavaMethod> fromValueMethod = item.tryGetMethod("fromValue", String.class);

                                if (valueMethod.isEmpty()) {
                                    violations.add("missing method value()");
                                } else {
                                    JavaMethod method = valueMethod.get();
                                    if (method.getModifiers().contains(JavaModifier.STATIC)) {
                                        violations.add("value() must be an instance method");
                                    }
                                    if (!String.class
                                            .getName()
                                            .equals(method.getRawReturnType().getFullName())) {
                                        violations.add("value() must return String");
                                    }
                                    if (!callsMethod(method, "name")) {
                                        violations.add("value() must delegate to name()");
                                    }
                                }

                                if (fromValueMethod.isPresent()) {
                                    violations.add("simple enum must not declare fromValue(String); use from(String)");
                                }

                                if (fromMethod.isEmpty()) {
                                    violations.add("missing static method from(String)");
                                } else {
                                    JavaMethod method = fromMethod.get();
                                    if (!method.getModifiers().contains(JavaModifier.STATIC)) {
                                        violations.add("from(String) must be static");
                                    }
                                    if (!method.getRawReturnType().equals(item)) {
                                        violations.add("from(String) must return " + item.getSimpleName());
                                    }
                                    if (!callsMethod(method, "values")) {
                                        violations.add("from(String) must call values()");
                                    }
                                    if (!callsMethod(method, "equalsIgnoreCase")) {
                                        violations.add("from(String) must call String.equalsIgnoreCase(..)");
                                    }
                                    if (!callsMethod(method, "orElseThrow")) {
                                        violations.add("from(String) must call Optional.orElseThrow(..)");
                                    }
                                }

                                List<String> extraMethods = item.getMethods().stream()
                                        .filter(method -> !method.getModifiers().contains(JavaModifier.SYNTHETIC))
                                        .filter(method -> !method.getModifiers().contains(JavaModifier.BRIDGE))
                                        .filter(method -> !isCompilerGeneratedEnumMethod(method))
                                        .filter(method -> !isAllowedSimpleEnumMethod(method))
                                        .map(NamingAndPlacementRuleSupport::formatMethod)
                                        .sorted()
                                        .toList();
                                if (!extraMethods.isEmpty()) {
                                    violations.add(
                                            "simple enum must only declare value() and static from(String); found extra methods "
                                                    + extraMethods);
                                }

                                boolean satisfied = violations.isEmpty();
                                String detail = satisfied
                                        ? item.getFullName() + " simple enum convention check passed"
                                        : item.getFullName() + " violation: " + String.join("; ", violations)
                                                + ". Fix: declare value() { return name(); } and static from(String value) using "
                                                + "Arrays.stream(values()).filter(item -> item.name().equalsIgnoreCase(value))"
                                                + ".findFirst().orElseThrow(...)";
                                events.add(new SimpleConditionEvent(item, satisfied, detail));
                            }
                        })
                .allowEmptyShould(true)
                .because("configured simple enums must expose value() -> name() and from(String)");
    }

    private static boolean matchesClassNamePattern(String pattern, String fullName) {
        if (!pattern.contains("*")) {
            return pattern.equals(fullName);
        }
        String[] segments = pattern.split("\\*", -1);
        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            regex.append(Pattern.quote(segments[i]));
            if (i < segments.length - 1) {
                regex.append(".*");
            }
        }
        return fullName.matches(regex.toString());
    }

    private static boolean callsMethod(JavaMethod method, String methodName) {
        return method.getMethodCallsFromSelf().stream()
                .map(JavaMethodCall::getTarget)
                .anyMatch(target -> methodName.equals(target.getName()));
    }

    private static String formatMethod(JavaClass owner, JavaMethod method) {
        String parameters = method.getRawParameterTypes().stream()
                .map(JavaClass::getSimpleName)
                .collect(Collectors.joining(", "));
        return owner.getFullName() + "#" + method.getName() + "(" + parameters + ")";
    }

    static Optional<Path> resolveSourceFilePath(Optional<Source> source, JavaClass item) {
        if (source.isPresent()) {
            Optional<Path> sourceFile = toSourceFilePath(source.get().getUri(), item);
            if (sourceFile.isPresent() && Files.exists(sourceFile.get())) {
                return sourceFile;
            }
        }
        return findWorkspaceSourceFile(item);
    }

    private static Optional<Path> toSourceFilePath(URI classFileUri, JavaClass item) {
        if (!"file".equalsIgnoreCase(classFileUri.getScheme())) {
            return Optional.empty();
        }
        Path classFilePath = Path.of(classFileUri);
        String packagePath = item.getPackageName().replace('.', '/');
        String relativeClassPath = packagePath.isEmpty()
                ? item.getSimpleName() + ".class"
                : packagePath + "/" + item.getSimpleName() + ".class";
        String classFilePathText = classFilePath.toString().replace('\\', '/');
        int classPathIndex = classFilePathText.lastIndexOf(relativeClassPath);
        if (classPathIndex < 0) {
            return Optional.empty();
        }
        String root = classFilePathText.substring(0, classPathIndex);
        String sourceRoot;
        if (root.endsWith("target/classes/")) {
            sourceRoot = root.substring(0, root.length() - "target/classes/".length()) + "src/main/java/";
        } else if (root.endsWith("target/test-classes/")) {
            sourceRoot = root.substring(0, root.length() - "target/test-classes/".length()) + "src/test/java/";
        } else {
            return Optional.empty();
        }
        String sourceFileName = item.getSourceCodeLocation().getSourceFileName();
        String relativeSourcePath = packagePath.isEmpty() ? sourceFileName : packagePath + "/" + sourceFileName;
        return Optional.of(Path.of(sourceRoot + relativeSourcePath));
    }

    private static Optional<Path> findWorkspaceSourceFile(JavaClass item) {
        try {
            String packagePath = item.getPackageName().replace('.', '/');
            String sourceFileName = item.getSourceCodeLocation().getSourceFileName();
            Path mainRelativePath =
                    Path.of("src", "main", "java").resolve(packagePath).resolve(sourceFileName);
            Path testRelativePath =
                    Path.of("src", "test", "java").resolve(packagePath).resolve(sourceFileName);
            Path searchRoot = Path.of("").toAbsolutePath().normalize();
            for (Path candidateRoot = searchRoot; candidateRoot != null; candidateRoot = candidateRoot.getParent()) {
                Optional<Path> mainSource = findWorkspaceFile(candidateRoot, mainRelativePath);
                if (mainSource.isPresent()) {
                    return mainSource;
                }
                Optional<Path> testSource = findWorkspaceFile(candidateRoot, testRelativePath);
                if (testSource.isPresent()) {
                    return testSource;
                }
            }
            return Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Path> findWorkspaceFile(Path workspaceRoot, Path relativePath) {
        Path directPath = workspaceRoot.resolve(relativePath);
        if (Files.exists(directPath)) {
            return Optional.of(directPath);
        }
        try (Stream<Path> paths = Files.find(
                workspaceRoot, 20, (path, attributes) -> attributes.isRegularFile() && path.endsWith(relativePath))) {
            return paths.findFirst();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static boolean isAllowedSimpleEnumMethod(JavaMethod method) {
        List<String> parameterTypes = method.getRawParameterTypes().stream()
                .map(JavaClass::getFullName)
                .toList();
        if ("value".equals(method.getName())) {
            return parameterTypes.isEmpty() && !method.getModifiers().contains(JavaModifier.STATIC);
        }
        if ("from".equals(method.getName())) {
            return parameterTypes.equals(List.of(String.class.getName()))
                    && method.getModifiers().contains(JavaModifier.STATIC);
        }
        return false;
    }

    private static boolean isCompilerGeneratedEnumMethod(JavaMethod method) {
        List<String> parameterTypes = method.getRawParameterTypes().stream()
                .map(JavaClass::getFullName)
                .toList();
        if ("values".equals(method.getName())) {
            return parameterTypes.isEmpty() && method.getModifiers().contains(JavaModifier.STATIC);
        }
        if ("valueOf".equals(method.getName())) {
            return parameterTypes.equals(List.of(String.class.getName()))
                    && method.getModifiers().contains(JavaModifier.STATIC);
        }
        return false;
    }

    private static String formatMethod(JavaMethod method) {
        String methodName = method.getName() + "("
                + method.getRawParameterTypes().stream()
                        .map(JavaClass::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")";
        return method.getModifiers().contains(JavaModifier.STATIC) ? "static " + methodName : methodName;
    }

    private static String domainName(String basePackage) {
        int index = basePackage.lastIndexOf('.');
        return index >= 0 ? basePackage.substring(index + 1) : basePackage;
    }

    private static String aggregatePrefix(String repositoryImplSimpleName) {
        String stem = repositoryImplSimpleName.substring(0, repositoryImplSimpleName.length() - "RepositoryImpl".length());
        for (int i = 1; i < stem.length(); i++) {
            if (Character.isUpperCase(stem.charAt(i))) {
                return stem.substring(0, i);
            }
        }
        return stem;
    }

    private static List<String> findSimpleTypeReferences(JavaClass item, String suffix) {
        try {
            Optional<Path> sourceFile = resolveSourceFilePath(item.getSource(), item);
            if (sourceFile.isEmpty()) {
                return List.of();
            }
            String source = Files.readString(sourceFile.get());
            return Pattern.compile("\\b([A-Z][A-Za-z0-9_]*" + suffix + ")\\b")
                    .matcher(source)
                    .results()
                    .map(result -> result.group(1))
                    .distinct()
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static final class RequestMappingPathPrefixCondition extends ArchCondition<JavaClass> {

        private final String requiredPrefix;
        private final String typeName;

        private RequestMappingPathPrefixCondition(String requiredPrefix, String typeName) {
            super(typeName + " must declare @RequestMapping with prefix " + requiredPrefix);
            this.requiredPrefix = requiredPrefix;
            this.typeName = typeName;
        }

        @Override
        public void check(JavaClass item, ConditionEvents events) {
            try {
                Optional<Path> sourceFile = resolveSourceFilePath(item.getSource(), item);
                if (sourceFile.isEmpty()) {
                    events.add(SimpleConditionEvent.violated(
                            item, item.getFullName() + " source file not found, cannot inspect @RequestMapping"));
                    return;
                }
                String source = Files.readString(sourceFile.get());
                int classIndex = source.indexOf("class " + item.getSimpleName());
                if (classIndex < 0) {
                    events.add(SimpleConditionEvent.violated(
                            item, item.getFullName() + " class declaration not found in source"));
                    return;
                }
                String classHeader = source.substring(0, classIndex);
                int requestMappingIndex = classHeader.lastIndexOf("@RequestMapping(");
                if (requestMappingIndex < 0) {
                    events.add(SimpleConditionEvent.violated(
                            item, typeName + " must declare class-level @RequestMapping"));
                    return;
                }
                int annotationEnd = classHeader.indexOf(")", requestMappingIndex);
                if (annotationEnd < 0) {
                    events.add(SimpleConditionEvent.violated(
                            item, item.getFullName() + " @RequestMapping annotation is not closed"));
                    return;
                }
                String requestMappingContent = classHeader.substring(requestMappingIndex, annotationEnd + 1);
                List<String> mappings = Pattern.compile("\"([^\"]+)\"")
                        .matcher(requestMappingContent)
                        .results()
                        .map(result -> result.group(1))
                        .filter(path -> !path.isBlank())
                        .toList();
                if (mappings.isEmpty()) {
                    events.add(SimpleConditionEvent.violated(
                            item, typeName + " must declare non-empty @RequestMapping path"));
                    return;
                }
                List<String> invalid = mappings.stream()
                        .filter(mapping -> !mapping.equals(requiredPrefix) && !mapping.startsWith(requiredPrefix + "/"))
                        .toList();
                if (invalid.isEmpty()) {
                    events.add(SimpleConditionEvent.satisfied(
                            item, item.getFullName() + " uses mapping " + mappings + " with prefix " + requiredPrefix));
                    return;
                }
                events.add(SimpleConditionEvent.violated(
                        item,
                        item.getFullName() + " mapping " + invalid + " does not match required prefix "
                                + requiredPrefix));
            } catch (Exception ex) {
                events.add(SimpleConditionEvent.violated(
                        item,
                        item.getFullName() + " cannot be checked for @RequestMapping prefix because "
                                + ex.getMessage()));
            }
        }
    }
}
