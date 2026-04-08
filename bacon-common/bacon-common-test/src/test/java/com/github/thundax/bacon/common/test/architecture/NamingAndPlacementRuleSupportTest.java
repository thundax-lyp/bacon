package com.github.thundax.bacon.common.test.architecture;

import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.EnumFieldFixture;
import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.InvalidSimpleEnumFixture;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamingAndPlacementRuleSupportTest {

    @Test
    void boundaryConstructorTypeShouldAllowEnumParameter() {
        assertThat(NamingAndPlacementRuleSupport.isBoundaryConstructorType(
                new ClassFileImporter().importClasses(BoundaryStatusFixture.class).get(BoundaryStatusFixture.class)))
                .isTrue();
    }

    @Test
    void entityConstructorRuleShouldRejectExplicitConstructors() {
        EvaluationResult result = evaluate(InvalidBoundaryTypeEntityFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(InvalidBoundaryTypeEntityFixture.class.getName() + " violation")
                .contains("Found 2 explicit constructors")
                .contains("expected 0 explicit constructors")
                .contains("move boundary conversion outside the entity");
    }

    @Test
    void entityConstructorRuleShouldRejectMultipleExplicitConstructors() {
        EvaluationResult result = evaluate(MultipleExplicitConstructorsEntityFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(MultipleExplicitConstructorsEntityFixture.class.getName() + " violation")
                .contains("Found 2 explicit constructors")
                .contains("expected 0 explicit constructors")
                .contains("move boundary conversion outside the entity");
    }

    @Test
    void entityBoundaryConstructorRuleShouldSupportWildcardClassPatterns() {
        EvaluationResult result = NamingAndPlacementRuleSupport
                .entityShouldUseSingleExplicitBoundaryConstructor(
                        NamingAndPlacementRuleSupportTest.class.getPackageName() + ".*EntityFixture")
                .evaluate(new ClassFileImporter().importPackages(NamingAndPlacementRuleSupportTest.class.getPackageName()));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains(InvalidBoundaryTypeEntityFixture.class.getName()))
                .anyMatch(detail -> detail.contains(MultipleExplicitConstructorsEntityFixture.class.getName()));
    }

    @Test
    void entityConstructorRuleShouldRequireAllArgsConstructorAnnotation() {
        EvaluationResult result = evaluate(MissingAllArgsConstructorEntityFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(MissingAllArgsConstructorEntityFixture.class.getName() + " violation")
                .contains("Class must be annotated with @AllArgsConstructor")
                .contains("expected 0 explicit constructors");
    }

    @Test
    void entityConstructorRuleShouldSkipRecordClasses() {
        EvaluationResult result = NamingAndPlacementRuleSupport
                .entityShouldUseSingleExplicitBoundaryConstructor(
                        RecordEntityFixture.class.getName(),
                        ValidAnnotatedEntityFixture.class.getName())
                .evaluate(new ClassFileImporter().importPackages(NamingAndPlacementRuleSupportTest.class.getPackageName()));

        assertThat(result.hasViolation()).isFalse();
    }

    @Test
    void resolveSourceFilePathShouldFallbackToWorkspaceLookupWhenClassSourceIsUnavailable() {
        JavaClass targetClass = new ClassFileImporter().importClasses(ValidAnnotatedEntityFixture.class)
                .get(ValidAnnotatedEntityFixture.class);

        Optional<Path> sourceFile = NamingAndPlacementRuleSupport.resolveSourceFilePath(Optional.empty(), targetClass);

        assertThat(sourceFile).isPresent();
        assertThat(sourceFile.orElseThrow())
                .endsWith(Path.of("src", "test", "java", "com", "github", "thundax", "bacon",
                        "common", "test", "architecture", "NamingAndPlacementRuleSupportTest.java"));
    }

    @Test
    void simpleEnumConventionShouldRejectFromValueMethod() {
        EvaluationResult result = evaluateSimpleEnum(InvalidSimpleEnumFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(InvalidSimpleEnumFixture.class.getName() + " violation")
                .contains("simple enum must not declare fromValue(String); use from(String)")
                .contains("missing static method from(String)")
                .contains("Fix: declare value() { return name(); } and static from(String value) using Arrays.stream(values())");
    }

    @Test
    void simpleEnumConventionShouldIgnoreEnumsWithInstanceFields() {
        EvaluationResult result = NamingAndPlacementRuleSupport
                .simpleEnumShouldUseNameAndFromConvention(EnumFieldFixture.class.getName())
                .evaluate(new ClassFileImporter().importPackages(EnumFieldFixture.class.getPackageName()));

        assertThat(result.hasViolation()).isFalse();
    }

    private static EvaluationResult evaluate(Class<?> targetClass) {
        return NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(targetClass.getName())
                .evaluate(new ClassFileImporter().importPackages(targetClass.getPackageName()));
    }

    private static EvaluationResult evaluateSimpleEnum(Class<?> targetClass) {
        return NamingAndPlacementRuleSupport
                .simpleEnumShouldUseNameAndFromConvention(targetClass.getName())
                .evaluate(new ClassFileImporter().importPackages(targetClass.getPackageName()));
    }

    private static String singleViolationDetail(EvaluationResult result) {
        List<String> details = result.getFailureReport().getDetails();
        assertThat(details).hasSize(1);
        return details.get(0);
    }
}

final class InvalidBoundaryTypeEntityFixture {

    private SampleIdFixture id;
    private SampleCodeFixture code;
    private Instant createdAt;

    public InvalidBoundaryTypeEntityFixture(SampleIdFixture id, SampleCodeFixture code, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.createdAt = createdAt;
    }

    public InvalidBoundaryTypeEntityFixture(Long id, UnsupportedBoundaryTypeFixture code, Instant createdAt) {
        this(SampleIdFixture.of(id), SampleCodeFixture.of(code.value()), createdAt);
    }
}

final class MultipleExplicitConstructorsEntityFixture {

    private SampleIdFixture id;
    private BoundaryStatusFixture status;
    private Instant createdAt;

    public MultipleExplicitConstructorsEntityFixture(SampleIdFixture id, BoundaryStatusFixture status, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public MultipleExplicitConstructorsEntityFixture(Long id, BoundaryStatusFixture status, Instant createdAt) {
        this(SampleIdFixture.of(id), status, createdAt);
    }

    public MultipleExplicitConstructorsEntityFixture(String id, BoundaryStatusFixture status, Instant createdAt) {
        this(SampleIdFixture.of(Long.valueOf(id)), status, createdAt);
    }
}

final class MissingAllArgsConstructorEntityFixture {

    private SampleIdFixture id;
    private BoundaryStatusFixture status;
    private Instant createdAt;

    private MissingAllArgsConstructorEntityFixture(SampleIdFixture id, BoundaryStatusFixture status, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public MissingAllArgsConstructorEntityFixture(Long id, BoundaryStatusFixture status, Instant createdAt) {
        this(SampleIdFixture.of(id), status, createdAt);
    }
}

@AllArgsConstructor
final class ValidAnnotatedEntityFixture {

    private SampleIdFixture id;
    private BoundaryStatusFixture status;
    private Instant createdAt;
}

record RecordEntityFixture(Long id, String code, Instant createdAt) {
}

final class SampleIdFixture {

    private final Long value;

    private SampleIdFixture(Long value) {
        this.value = value;
    }

    static SampleIdFixture of(Long value) {
        return value == null ? null : new SampleIdFixture(value);
    }
}

final class SampleCodeFixture {

    private final String value;

    private SampleCodeFixture(String value) {
        this.value = value;
    }

    static SampleCodeFixture of(String value) {
        return value == null ? null : new SampleCodeFixture(value);
    }
}

final class UnsupportedBoundaryTypeFixture {

    private final String value;

    UnsupportedBoundaryTypeFixture(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}

enum BoundaryStatusFixture {
    ENABLED,
    DISABLED
}
