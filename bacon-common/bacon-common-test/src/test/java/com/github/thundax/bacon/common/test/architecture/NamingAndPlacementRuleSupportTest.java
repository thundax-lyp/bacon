package com.github.thundax.bacon.common.test.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.EnumFieldFixture;
import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.InvalidSimpleEnumFixture;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

class NamingAndPlacementRuleSupportTest {

    @Test
    void resolveSourceFilePathShouldFallbackToWorkspaceLookupWhenClassSourceIsUnavailable() {
        JavaClass targetClass = new ClassFileImporter()
                .importClasses(ValidAnnotatedEntityFixture.class)
                .get(ValidAnnotatedEntityFixture.class);

        Optional<Path> sourceFile = NamingAndPlacementRuleSupport.resolveSourceFilePath(Optional.empty(), targetClass);

        assertThat(sourceFile).isPresent();
        assertThat(sourceFile.orElseThrow())
                .endsWith(Path.of(
                        "src",
                        "test",
                        "java",
                        "com",
                        "github",
                        "thundax",
                        "bacon",
                        "common",
                        "test",
                        "architecture",
                        "NamingAndPlacementRuleSupportTest.java"));
    }

    @Test
    void simpleEnumConventionShouldRejectFromValueMethod() {
        EvaluationResult result = evaluateSimpleEnum(InvalidSimpleEnumFixture.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(singleViolationDetail(result))
                .contains(InvalidSimpleEnumFixture.class.getName() + " violation")
                .contains("simple enum must not declare fromValue(String); use from(String)")
                .contains("missing static method from(String)")
                .contains(
                        "Fix: declare value() { return name(); } and static from(String value) using Arrays.stream(values())");
    }

    @Test
    void simpleEnumConventionShouldIgnoreEnumsWithInstanceFields() {
        EvaluationResult result = NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(
                        EnumFieldFixture.class.getName())
                .evaluate(new ClassFileImporter().importPackages(EnumFieldFixture.class.getPackageName()));

        assertThat(result.hasViolation()).isFalse();
    }

    private static EvaluationResult evaluateSimpleEnum(Class<?> targetClass) {
        return NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(targetClass.getName())
                .evaluate(new ClassFileImporter().importPackages(targetClass.getPackageName()));
    }

    private static String singleViolationDetail(EvaluationResult result) {
        List<String> details = result.getFailureReport().getDetails();
        assertThat(details).hasSize(1);
        return details.get(0);
    }
}

@AllArgsConstructor
final class ValidAnnotatedEntityFixture {

    private String id;
}
