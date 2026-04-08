package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class NamingAndPlacementRuleSupport {

    private static final Set<String> BOUNDARY_CONSTRUCTOR_TYPES = Set.of(
            String.class.getName(),
            Long.class.getName(),
            Integer.class.getName(),
            Instant.class.getName()
    );

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
                .should().resideInAPackage(basePackage + ".infra.repository.impl..")
                .allowEmptyShould(true)
                .because("RepositoryImpl -> infra.repository.impl");
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
                .because("Persistence objects must use the DO suffix; do not use DataObject.");
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

    public static ArchRule entityShouldUseSingleExplicitBoundaryConstructor(String... fullyQualifiedClassNames) {
        Set<String> classNames = Set.of(fullyQualifiedClassNames);
        return ArchRuleDefinition.classes()
                .that(new DescribedPredicate<>("match configured entity classes") {
                    @Override
                    public boolean test(JavaClass input) {
                        return classNames.contains(input.getFullName());
                    }
                })
                .should(new ArchCondition<>("have a valid explicit boundary constructor") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        List<JavaConstructor> publicConstructors = item.getConstructors().stream()
                                .filter(constructor -> constructor.getModifiers().contains(JavaModifier.PUBLIC))
                                .toList();
                        List<String> allFieldTypes = nonStaticFieldTypeNames(item);
                        List<JavaConstructor> explicitConstructors = publicConstructors.stream()
                                .filter(constructor -> !constructor.getRawParameterTypes().isEmpty())
                                .filter(constructor -> !sameSignature(constructor, allFieldTypes))
                                .toList();
                        boolean singleExplicitConstructor = explicitConstructors.size() == 1;
                        JavaConstructor explicitConstructor = singleExplicitConstructor ? explicitConstructors.get(0) : null;
                        List<String> invalidBoundaryTypes = explicitConstructor == null
                                ? List.of()
                                : explicitConstructor.getRawParameterTypes().stream()
                                        .filter(parameter -> !isBoundaryConstructorType(parameter))
                                        .map(JavaClass::getFullName)
                                        .toList();
                        boolean boundaryTypesOnly = singleExplicitConstructor
                                && invalidBoundaryTypes.isEmpty();
                        boolean delegatesToOwnConstructor = singleExplicitConstructor
                                && explicitConstructor.getCallsOfSelf().stream()
                                .anyMatch(call -> call.getTargetOwner().equals(item));
                        boolean satisfied = singleExplicitConstructor && boundaryTypesOnly && delegatesToOwnConstructor;
                        String detail = satisfied
                                ? item.getFullName() + " entity boundary constructor check passed"
                                : buildBoundaryConstructorFailureMessage(
                                        item, explicitConstructors, invalidBoundaryTypes, delegatesToOwnConstructor);
                        events.add(new SimpleConditionEvent(item, satisfied, detail));
                    }
                })
                .allowEmptyShould(false)
                .because("violations should explain the exact failure reason and the correct constructor pattern");
    }

    private static boolean sameSignature(JavaConstructor constructor, List<String> fieldTypeNames) {
        List<String> parameterTypeNames = constructor.getRawParameterTypes().stream()
                .map(JavaClass::getFullName)
                .toList();
        return parameterTypeNames.equals(fieldTypeNames);
    }

    static boolean isBoundaryConstructorType(JavaClass parameterType) {
        return BOUNDARY_CONSTRUCTOR_TYPES.contains(parameterType.getFullName()) || parameterType.isEnum();
    }

    private static String buildBoundaryConstructorFailureMessage(
            JavaClass item,
            List<JavaConstructor> explicitConstructors,
            List<String> invalidBoundaryTypes,
            boolean delegatesToOwnConstructor) {
        List<String> reasons = new ArrayList<>();
        if (explicitConstructors.size() != 1) {
            reasons.add("Found " + explicitConstructors.size() + " explicit constructors"
                    + formatConstructors(explicitConstructors) + "; expected exactly 1 explicit boundary constructor");
        }
        if (explicitConstructors.size() == 1 && !invalidBoundaryTypes.isEmpty()) {
            reasons.add("Explicit boundary constructor " + formatConstructor(explicitConstructors.get(0))
                    + " uses unsupported parameter types " + invalidBoundaryTypes
                    + "; allowed types are String, Long, Integer, Instant, and enum");
        }
        if (explicitConstructors.size() == 1 && !delegatesToOwnConstructor) {
            reasons.add("Explicit boundary constructor " + formatConstructor(explicitConstructors.get(0))
                    + " does not delegate to the all-fields constructor via this(...)");
        }
        return item.getFullName() + " violation: " + String.join("; ", reasons)
                + ". Fix: " + suggestedBoundaryConstructor(item);
    }

    private static String formatConstructors(List<JavaConstructor> constructors) {
        if (constructors.isEmpty()) {
            return "";
        }
        return ": " + constructors.stream()
                .map(NamingAndPlacementRuleSupport::formatConstructor)
                .collect(Collectors.joining(" / "));
    }

    private static String formatConstructor(JavaConstructor constructor) {
        return constructor.getOwner().getSimpleName() + "(" + constructor.getRawParameterTypes().stream()
                .map(JavaClass::getSimpleName)
                .collect(Collectors.joining(", ")) + ")";
    }

    private static String suggestedBoundaryConstructor(JavaClass item) {
        String parameters = nonStaticFields(item).stream()
                .map(field -> inferBoundaryParameterTypeName(field.getType()) + " " + field.getName())
                .collect(Collectors.joining(", "));
        return item.getSimpleName() + "(" + parameters + ") {...}";
    }

    private static String inferBoundaryParameterTypeName(Class<?> fieldType) {
        Class<?> normalizedType = wrapPrimitiveType(fieldType);
        if (isBoundaryConstructorTypeName(normalizedType.getName()) || normalizedType.isEnum()) {
            return normalizedType.getSimpleName();
        }
        if (inheritsFrom(normalizedType, "com.github.thundax.bacon.common.id.core.BaseLongId")) {
            return Long.class.getSimpleName();
        }
        if (inheritsFrom(normalizedType, "com.github.thundax.bacon.common.id.core.BaseStringId")) {
            return String.class.getSimpleName();
        }
        return findPreferredBoundaryFactoryParameterType(normalizedType)
                .map(Class::getSimpleName)
                .orElse(normalizedType.getSimpleName());
    }

    private static Optional<Class<?>> findPreferredBoundaryFactoryParameterType(Class<?> fieldType) {
        Class<?> bestMatch = null;
        for (java.lang.reflect.Method method : fieldType.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (!fieldType.equals(method.getReturnType())) {
                continue;
            }
            Class<?> parameterType = wrapPrimitiveType(method.getParameterTypes()[0]);
            if (!isBoundaryConstructorTypeName(parameterType.getName()) && !parameterType.isEnum()) {
                continue;
            }
            if (bestMatch == null
                    || boundaryParameterPriority(parameterType) < boundaryParameterPriority(bestMatch)) {
                bestMatch = parameterType;
            }
        }
        return Optional.ofNullable(bestMatch);
    }

    private static int boundaryParameterPriority(Class<?> parameterType) {
        if (Long.class.equals(parameterType)) {
            return 0;
        }
        if (Integer.class.equals(parameterType)) {
            return 1;
        }
        if (Instant.class.equals(parameterType)) {
            return 2;
        }
        if (parameterType.isEnum()) {
            return 3;
        }
        if (String.class.equals(parameterType)) {
            return 4;
        }
        return 5;
    }

    private static boolean inheritsFrom(Class<?> fieldType, String expectedSuperclassName) {
        Class<?> current = fieldType;
        while (current != null) {
            if (expectedSuperclassName.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static boolean isBoundaryConstructorTypeName(String typeName) {
        return BOUNDARY_CONSTRUCTOR_TYPES.contains(typeName);
    }

    private static Class<?> wrapPrimitiveType(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (Long.TYPE.equals(type)) {
            return Long.class;
        }
        if (Integer.TYPE.equals(type)) {
            return Integer.class;
        }
        return type;
    }

    private static List<String> nonStaticFieldTypeNames(JavaClass javaClass) {
        return nonStaticFields(javaClass).stream()
                .map(Field::getType)
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    private static List<Field> nonStaticFields(JavaClass javaClass) {
        return Arrays.stream(javaClass.reflect().getDeclaredFields())
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .toList();
    }
}
