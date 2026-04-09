package com.github.thundax.bacon.boot;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.infra.facade.remote.OAuthClientReadFacadeRemoteImpl;
import com.github.thundax.bacon.auth.infra.facade.remote.SessionCommandFacadeRemoteImpl;
import com.github.thundax.bacon.auth.infra.facade.remote.TokenVerifyFacadeRemoteImpl;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.test.BaconSpringBootTest;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.infra.facade.remote.impl.InventoryCommandFacadeRemoteImpl;
import com.github.thundax.bacon.inventory.infra.facade.remote.impl.InventoryReadFacadeRemoteImpl;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.infra.facade.remote.OrderCommandFacadeRemoteImpl;
import com.github.thundax.bacon.order.infra.facade.remote.OrderReadFacadeRemoteImpl;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.infra.facade.remote.PaymentCommandFacadeRemoteImpl;
import com.github.thundax.bacon.payment.infra.facade.remote.PaymentReadFacadeRemoteImpl;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.infra.facade.remote.DepartmentReadFacadeRemoteImpl;
import com.github.thundax.bacon.upms.infra.facade.remote.PermissionReadFacadeRemoteImpl;
import com.github.thundax.bacon.upms.infra.facade.remote.RoleReadFacadeRemoteImpl;
import com.github.thundax.bacon.upms.infra.facade.remote.UserReadFacadeRemoteImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        classes = BaconMonoAssemblyTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "bacon.runtime.mode=micro",
            "spring.profiles.active=test",
            "spring.cloud.nacos.discovery.enabled=false",
            "spring.cloud.nacos.config.enabled=false",
            "spring.boot.admin.client.enabled=false",
            "spring.main.lazy-initialization=true",
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                    + "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration,"
                    + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "bacon.remote.auth-base-url=http://127.0.0.1:18083",
            "bacon.remote.upms-base-url=http://127.0.0.1:18082",
            "bacon.remote.order-base-url=http://127.0.0.1:18084",
            "bacon.remote.inventory-base-url=http://127.0.0.1:18085",
            "bacon.remote.payment-base-url=http://127.0.0.1:18086"
        })
@Import({
    OrderRepositoryTestConfiguration.class,
    PaymentRepositoryTestConfiguration.class,
    AuthRepositoryTestConfiguration.class,
    UpmsRepositoryTestConfiguration.class,
    StorageRepositoryTestConfiguration.class,
    MybatisMapperTestConfiguration.class
})
class BaconMonoBootMicroAssemblyTest extends BaconSpringBootTest {

    @Autowired
    private TokenVerifyFacade tokenVerifyFacade;

    @Autowired
    private SessionCommandFacade sessionCommandFacade;

    @Autowired
    private OAuthClientReadFacade oAuthClientReadFacade;

    @Autowired
    private UserReadFacade userReadFacade;

    @Autowired
    private DepartmentReadFacade departmentReadFacade;

    @Autowired
    private RoleReadFacade roleReadFacade;

    @Autowired
    private PermissionReadFacade permissionReadFacade;

    @Autowired
    private OrderReadFacade orderReadFacade;

    @Autowired
    private OrderCommandFacade orderCommandFacade;

    @Autowired
    private InventoryReadFacade inventoryReadFacade;

    @Autowired
    private InventoryCommandFacade inventoryCommandFacade;

    @Autowired
    private PaymentReadFacade paymentReadFacade;

    @Autowired
    private PaymentCommandFacade paymentCommandFacade;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Test
    void shouldWireRemoteFacadeImplementationsInMicroMode() {
        assertThat(tokenVerifyFacade).isInstanceOf(TokenVerifyFacadeRemoteImpl.class);
        assertThat(sessionCommandFacade).isInstanceOf(SessionCommandFacadeRemoteImpl.class);
        assertThat(oAuthClientReadFacade).isInstanceOf(OAuthClientReadFacadeRemoteImpl.class);
        assertThat(userReadFacade).isInstanceOf(UserReadFacadeRemoteImpl.class);
        assertThat(departmentReadFacade).isInstanceOf(DepartmentReadFacadeRemoteImpl.class);
        assertThat(roleReadFacade).isInstanceOf(RoleReadFacadeRemoteImpl.class);
        assertThat(permissionReadFacade).isInstanceOf(PermissionReadFacadeRemoteImpl.class);
        assertThat(orderReadFacade).isInstanceOf(OrderReadFacadeRemoteImpl.class);
        assertThat(orderCommandFacade).isInstanceOf(OrderCommandFacadeRemoteImpl.class);
        assertThat(inventoryReadFacade).isInstanceOf(InventoryReadFacadeRemoteImpl.class);
        assertThat(inventoryCommandFacade).isInstanceOf(InventoryCommandFacadeRemoteImpl.class);
        assertThat(paymentReadFacade).isInstanceOf(PaymentReadFacadeRemoteImpl.class);
        assertThat(paymentCommandFacade).isInstanceOf(PaymentCommandFacadeRemoteImpl.class);
        assertThat(currentUserProvider.currentUserId()).isEqualTo("system");
    }
}
