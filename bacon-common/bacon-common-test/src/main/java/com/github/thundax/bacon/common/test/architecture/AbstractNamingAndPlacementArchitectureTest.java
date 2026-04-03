package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractNamingAndPlacementArchitectureTest {

    private JavaClasses classes;

    protected abstract String basePackage();

    @BeforeAll
    void loadClasses() {
        this.classes = NamingAndPlacementRuleSupport.importDomainClasses(basePackage());
    }

    protected JavaClasses classes() {
        return classes;
    }

    @Test
    @DisplayName("Controller：对外业务 HTTP 入口，命名 {业务对象}{动作}Controller，目录 interfaces/controller/")
    void shouldFollowControllerRule() {
        NamingAndPlacementRuleSupport.controllerShouldUseControllerNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("ProviderController：对内服务 HTTP 入口，命名 {业务对象}{动作}ProviderController，目录 interfaces/provider/")
    void shouldFollowProviderControllerRule() {
        NamingAndPlacementRuleSupport.providerControllerShouldUseProviderControllerNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("Resolver：接口层请求解析辅助对象，命名 {业务对象}{动作}Resolver，目录 interfaces/resolver/")
    void shouldFollowResolverRule() {
        NamingAndPlacementRuleSupport.resolverShouldUseResolverNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("ApplicationService：业务用例编排入口，命名以 ApplicationService 结尾，目录 application/command|query|audit")
    void shouldFollowApplicationServiceRule() {
        NamingAndPlacementRuleSupport.applicationServiceShouldUseApplicationServiceNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("DomainService：封装领域规则，命名 {业务对象}DomainService，目录 domain/service/")
    void shouldFollowDomainServiceRule() {
        NamingAndPlacementRuleSupport.domainServiceShouldUseDomainServiceNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("Repository：领域仓储接口，命名 {业务对象}Repository，目录 domain/repository/")
    void shouldFollowRepositoryRule() {
        NamingAndPlacementRuleSupport.repositoryShouldUseRepositoryNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("RepositoryImpl：仓储实现，命名 {业务对象}RepositoryImpl，目录 infra/repository/impl/")
    void shouldFollowRepositoryImplRule() {
        NamingAndPlacementRuleSupport.repositoryImplShouldUseRepositoryImplNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("Mapper：持久化映射，命名 {业务对象}Mapper，目录 infra/persistence/mapper/")
    void shouldFollowMapperRule() {
        NamingAndPlacementRuleSupport.mapperShouldUseMapperNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("Converter：对象转换，命名 {业务对象}Converter，目录 infra/repository/converter/")
    void shouldFollowConverterRule() {
        NamingAndPlacementRuleSupport.converterShouldUseConverterNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("Facade：跨域调用契约，命名 {业务对象}{动作}Facade，目录 api/facade/")
    void shouldFollowFacadeRule() {
        NamingAndPlacementRuleSupport.facadeShouldUseFacadeNameAndPackage(basePackage()).check(classes());
    }

    @Test
    @DisplayName("FacadeLocalImpl：单体模式门面实现，命名 {业务对象}{动作}FacadeLocalImpl，目录 interfaces/facade/")
    void shouldFollowFacadeLocalImplRule() {
        NamingAndPlacementRuleSupport.facadeLocalImplShouldUseFacadeLocalImplNameAndPackage(basePackage())
                .check(classes());
    }

    @Test
    @DisplayName("FacadeRemoteImpl：微服务模式门面实现，命名 {业务对象}{动作}FacadeRemoteImpl，目录 infra/facade/remote/")
    void shouldFollowFacadeRemoteImplRule() {
        NamingAndPlacementRuleSupport.facadeRemoteImplShouldUseFacadeRemoteImplNameAndPackage(basePackage())
                .check(classes());
    }
}
