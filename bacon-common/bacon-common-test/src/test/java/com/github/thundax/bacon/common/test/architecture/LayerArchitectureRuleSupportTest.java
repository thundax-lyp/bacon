package com.github.thundax.bacon.common.test.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.test.architecture.fixture.contract.common.support.CommonSupport;
import com.github.thundax.bacon.common.test.architecture.fixture.contract.order.api.facade.InvalidOrderFacadeDependsOnPaymentApplication;
import com.github.thundax.bacon.common.test.architecture.fixture.contract.order.api.facade.ValidOrderFacadeDependsOnCommon;
import com.github.thundax.bacon.common.test.architecture.fixture.contract.payment.application.service.PaymentApplicationService;
import com.github.thundax.bacon.common.test.architecture.fixture.layered.api.facade.InvalidDomainDependentFacade;
import com.github.thundax.bacon.common.test.architecture.fixture.layered.api.facade.ValidFixtureFacade;
import com.github.thundax.bacon.common.test.architecture.fixture.layered.api.request.ValidFixtureRequest;
import com.github.thundax.bacon.common.test.architecture.fixture.layered.domain.model.valueobject.InvalidFixtureId;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

class LayerArchitectureRuleSupportTest {

    @Test
    void apiShouldAcceptClassesThatDoNotDependOnDomain() {
        EvaluationResult result = evaluateApiDomainRule(ValidFixtureFacade.class, ValidFixtureRequest.class);

        assertThat(result.hasViolation()).isFalse();
    }

    @Test
    void apiShouldRejectClassesThatDependOnDomain() {
        EvaluationResult result = evaluateApiDomainRule(InvalidDomainDependentFacade.class, InvalidFixtureId.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains(InvalidDomainDependentFacade.class.getName())
                        && detail.contains(InvalidFixtureId.class.getName()));
    }

    @Test
    void apiShouldAcceptDependenciesOnCommonPackage() {
        EvaluationResult result =
                evaluateApiOtherDomainModuleRule(ValidOrderFacadeDependsOnCommon.class, CommonSupport.class);

        assertThat(result.hasViolation()).isFalse();
    }

    @Test
    void apiShouldRejectDependenciesOnOtherDomainModules() {
        EvaluationResult result = evaluateApiOtherDomainModuleRule(
                InvalidOrderFacadeDependsOnPaymentApplication.class, PaymentApplicationService.class);

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().getDetails())
                .anyMatch(detail -> detail.contains(InvalidOrderFacadeDependsOnPaymentApplication.class.getName())
                        && detail.contains(PaymentApplicationService.class.getName()));
    }

    private static EvaluationResult evaluateApiDomainRule(Class<?>... targetClasses) {
        String basePackage = "com.github.thundax.bacon.common.test.architecture.fixture.layered";
        return LayerArchitectureRuleSupport.apiShouldNotDependOnAnyDomain(basePackage)
                .evaluate(new ClassFileImporter().importClasses(targetClasses));
    }

    private static EvaluationResult evaluateApiOtherDomainModuleRule(Class<?>... targetClasses) {
        String basePackage = "com.github.thundax.bacon.common.test.architecture.fixture.contract.order";
        return LayerArchitectureRuleSupport.apiShouldNotDependOnAnyOtherDomainModules(basePackage)
                .evaluate(new ClassFileImporter().importClasses(targetClasses));
    }
}
