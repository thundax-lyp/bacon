package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import java.time.Instant;
import java.util.List;
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
    void entityBoundaryConstructorViolationShouldExplainInvalidBoundaryTypeAndCorrectWriting() {
        EvaluationResult result = evaluate(InvalidBoundaryTypeEntityFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(InvalidBoundaryTypeEntityFixture.class.getName() + " violation")
                .contains("uses unsupported parameter types")
                .contains(UnsupportedBoundaryTypeFixture.class.getName())
                .contains("Fix: InvalidBoundaryTypeEntityFixture(Long id, String code, Instant createdAt) {...}")
                .doesNotContain("explicitConstructors=");
    }

    @Test
    void entityBoundaryConstructorViolationShouldExplainMultipleExplicitConstructorsAndCorrectWriting() {
        EvaluationResult result = evaluate(MultipleExplicitConstructorsEntityFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(MultipleExplicitConstructorsEntityFixture.class.getName() + " violation")
                .contains("Found 2 explicit constructors")
                .contains("MultipleExplicitConstructorsEntityFixture(Long, BoundaryStatusFixture, Instant)")
                .contains("MultipleExplicitConstructorsEntityFixture(String, BoundaryStatusFixture, Instant)")
                .contains("Fix: MultipleExplicitConstructorsEntityFixture(Long id, BoundaryStatusFixture status, Instant createdAt) {...}")
                .doesNotContain("explicitConstructors=");
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

    private static EvaluationResult evaluate(Class<?> targetClass) {
        return NamingAndPlacementRuleSupport.entityShouldUseSingleExplicitBoundaryConstructor(targetClass.getName())
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
