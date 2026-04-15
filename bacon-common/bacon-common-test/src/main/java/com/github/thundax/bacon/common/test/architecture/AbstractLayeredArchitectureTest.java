package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractLayeredArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = LayeredArchitectureRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    @DisplayName("默认分层依赖方向：domain/application/interfaces/infra 只能沿既定方向依赖")
    void shouldFollowDefaultDirection() {
        LayeredArchitectureRuleSupport.domainShouldNotDependOnOuterLayers(basePackage())
                .check(classes());
        LayeredArchitectureRuleSupport.applicationShouldNotDependOnInterfacesOrOwnInfra(basePackage())
                .check(classes());
        LayeredArchitectureRuleSupport.interfacesShouldNotDependOnOwnInfra(basePackage())
                .check(classes());
        LayeredArchitectureRuleSupport.infraShouldNotDependOnApplicationOrInterfaces(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("interfaces 不得直接依赖 infra.persistence.mapper")
    void shouldKeepInterfacesAwayFromPersistenceMapper() {
        LayeredArchitectureRuleSupport.interfacesShouldNotDependOnPersistenceMapper(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("interfaces 不得直接依赖其他业务域的 infra")
    void shouldKeepInterfacesAwayFromOtherDomainInfra() {
        LayeredArchitectureRuleSupport.interfacesShouldNotDependOnOtherDomainInfra(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("application 不得依赖本域或他域的 infra")
    void shouldKeepApplicationAwayFromInfra() {
        LayeredArchitectureRuleSupport.applicationShouldNotDependOnAnyDomainInfra(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("domain 不得依赖 Spring MVC、MyBatis、HTTP client、Redis、MQ 等技术包")
    void shouldKeepDomainAwayFromTechnicalPackages() {
        LayeredArchitectureRuleSupport.domainShouldNotDependOnTechnicalPackages(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("@SysLog 只能出现在 interfaces.controller")
    void shouldKeepSysLogInInterfacesController() {
        LayeredArchitectureRuleSupport.sysLogShouldOnlyAppearInInterfacesController(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("@Transactional 默认只允许出现在 application")
    void shouldKeepTransactionalInApplication() {
        LayeredArchitectureRuleSupport.transactionalShouldOnlyAppearInApplication(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("infra 只能作为实现层依赖 domain.repository")
    void shouldKeepDomainRepositoryDependencyInsideInfraRepositoryImpl() {
        LayeredArchitectureRuleSupport.infraShouldOnlyDependOnDomainRepositoryAsImplementation(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("domain entity 的 create() 只能由 application 调用")
    void shouldOnlyAllowApplicationToCallDomainEntityCreate() {
        LayeredArchitectureRuleSupport.domainEntityCreateShouldOnlyBeCalledByApplication(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("domain entity 的 reconstruct() 只能由 infra 调用")
    void shouldOnlyAllowInfraToCallDomainEntityReconstruct() {
        LayeredArchitectureRuleSupport.domainEntityReconstructShouldOnlyBeCalledByInfra(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("application 公共方法不得直接暴露 protocol model 或 PageQueryDTO")
    void shouldKeepApplicationPublicMethodContractsStable() {
        LayeredArchitectureRuleSupport.applicationPublicMethodsShouldNotUseProtocolModels(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("ApplicationService 不得本地 toDto 或直接 new DTO，应使用 application.assembler")
    void shouldKeepDtoMappingInsideApplicationAssembler() {
        LayeredArchitectureRuleSupport.applicationServicesShouldUseAssemblersForDtoMapping(basePackage())
                .check(classes());
    }
}
