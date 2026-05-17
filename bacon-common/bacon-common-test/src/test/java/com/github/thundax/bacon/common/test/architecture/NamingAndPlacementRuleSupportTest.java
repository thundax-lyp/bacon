package com.github.thundax.bacon.common.test.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.EnumFieldFixture;
import com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums.InvalidSimpleEnumFixture;
import com.github.thundax.bacon.common.test.architecture.fixture.domain.repository.InvalidFixtureRepository;
import com.github.thundax.bacon.common.test.architecture.fixture.domain.repository.ValidFixtureRepository;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.dto.InvalidFixtureDTO;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.facade.InvalidFixtureFacade;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.facade.ValidFixtureFacade;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.request.ValidFixtureFacadeRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.facade.api.response.ValidFixtureFacadeResponse;
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

    @Test
    void facadeContractShouldAcceptFacadeRequestAndFacadeResponse() {
        EvaluationResult requestResult = evaluateFacadeRequestContract(
                ValidFixtureFacade.class, ValidFixtureFacadeRequest.class, ValidFixtureFacadeResponse.class);
        EvaluationResult responseResult = evaluateFacadeResponseContract(
                ValidFixtureFacade.class, ValidFixtureFacadeRequest.class, ValidFixtureFacadeResponse.class);

        assertThat(requestResult.hasViolation()).isFalse();
        assertThat(responseResult.hasViolation()).isFalse();
    }

    @Test
    void facadeContractShouldAcceptVoidReturnAndNoParameter() {
        EvaluationResult requestResult = evaluateFacadeRequestContract(VoidFixtureFacade.class);
        EvaluationResult responseResult = evaluateFacadeResponseContract(VoidFixtureFacade.class);

        assertThat(requestResult.hasViolation()).isFalse();
        assertThat(responseResult.hasViolation()).isFalse();
    }

    @Test
    void facadeRequestContractShouldRejectNonFacadeRequest() {
        EvaluationResult result = evaluateFacadeRequestContract(
                InvalidFixtureFacade.class, ValidFixtureFacadeRequest.class, InvalidFixtureDTO.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains("InvalidFixtureFacade#listByKeyword(String)")
                        && detail.contains("parameters must be empty or use a single")
                        && detail.contains(".api.request.*FacadeRequest"));
    }

    @Test
    void facadeResponseContractShouldRejectNonFacadeResponse() {
        EvaluationResult result = evaluateFacadeResponseContract(
                InvalidFixtureFacade.class, ValidFixtureFacadeRequest.class, InvalidFixtureDTO.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains("InvalidFixtureFacade#queryById(ValidFixtureFacadeRequest)")
                        && detail.contains("return type must be void or use")
                        && detail.contains(".api.response.*FacadeResponse"))
                .anyMatch(detail -> detail.contains("InvalidFixtureFacade#listByKeyword(String)")
                        && detail.contains("return type must be void or use")
                        && detail.contains(".api.response.*FacadeResponse"));
    }

    @Test
    void repositoryMethodPrefixShouldAcceptWhitelistedPrefixes() {
        EvaluationResult result = evaluateRepositoryMethodPrefix(ValidFixtureRepository.class);

        assertThat(result.hasViolation()).isFalse();
    }

    @Test
    void repositoryMethodPrefixShouldRejectNonWhitelistedPrefix() {
        EvaluationResult result = evaluateRepositoryMethodPrefix(InvalidFixtureRepository.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains("InvalidFixtureRepository#saveFixture()")
                        && detail.contains("repository method prefix must be one of"));
    }

    private static EvaluationResult evaluateSimpleEnum(Class<?> targetClass) {
        return NamingAndPlacementRuleSupport.simpleEnumShouldUseNameAndFromConvention(targetClass.getName())
                .evaluate(new ClassFileImporter().importPackages(targetClass.getPackageName()));
    }

    private static EvaluationResult evaluateFacadeRequestContract(Class<?>... targetClasses) {
        String basePackage = "com.github.thundax.bacon.common.test.architecture.fixture.facade";
        return NamingAndPlacementRuleSupport.facadeMethodShouldUseSingleFacadeRequest(basePackage)
                .evaluate(new ClassFileImporter().importClasses(targetClasses));
    }

    private static EvaluationResult evaluateFacadeResponseContract(Class<?>... targetClasses) {
        String basePackage = "com.github.thundax.bacon.common.test.architecture.fixture.facade";
        return NamingAndPlacementRuleSupport.facadeMethodShouldUseFacadeResponse(basePackage)
                .evaluate(new ClassFileImporter().importClasses(targetClasses));
    }

    private static EvaluationResult evaluateRepositoryMethodPrefix(Class<?>... targetClasses) {
        String basePackage = "com.github.thundax.bacon.common.test.architecture.fixture";
        return NamingAndPlacementRuleSupport.repositoryMethodShouldUseWhitelistedPrefix(basePackage)
                .evaluate(new ClassFileImporter().importClasses(targetClasses));
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

interface VoidFixtureFacade {

    void refresh();
}
