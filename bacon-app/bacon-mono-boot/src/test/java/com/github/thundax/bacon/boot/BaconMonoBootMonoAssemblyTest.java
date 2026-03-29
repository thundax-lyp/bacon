package com.github.thundax.bacon.boot;

import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.interfaces.facade.OAuthClientReadFacadeLocalImpl;
import com.github.thundax.bacon.auth.interfaces.facade.SessionCommandFacadeLocalImpl;
import com.github.thundax.bacon.auth.interfaces.facade.TokenVerifyFacadeLocalImpl;
import com.github.thundax.bacon.common.security.context.CurrentUserProvider;
import com.github.thundax.bacon.common.security.context.MonoCurrentUserProvider;
import com.github.thundax.bacon.common.test.BaconSpringBootTest;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.interfaces.facade.InventoryCommandFacadeLocalImpl;
import com.github.thundax.bacon.inventory.interfaces.facade.InventoryReadFacadeLocalImpl;
import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.interfaces.facade.OrderCommandFacadeLocalImpl;
import com.github.thundax.bacon.order.interfaces.facade.OrderReadFacadeLocalImpl;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.facade.PaymentReadFacade;
import com.github.thundax.bacon.payment.interfaces.facade.PaymentCommandFacadeLocalImpl;
import com.github.thundax.bacon.payment.interfaces.facade.PaymentReadFacadeLocalImpl;
import com.github.thundax.bacon.upms.api.facade.DepartmentReadFacade;
import com.github.thundax.bacon.upms.api.facade.PermissionReadFacade;
import com.github.thundax.bacon.upms.api.facade.RoleReadFacade;
import com.github.thundax.bacon.upms.api.facade.UserReadFacade;
import com.github.thundax.bacon.upms.interfaces.facade.DepartmentReadFacadeLocalImpl;
import com.github.thundax.bacon.upms.interfaces.facade.PermissionReadFacadeLocalImpl;
import com.github.thundax.bacon.upms.interfaces.facade.RoleReadFacadeLocalImpl;
import com.github.thundax.bacon.upms.interfaces.facade.UserReadFacadeLocalImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BaconMonoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "bacon.runtime.mode=mono",
                "spring.profiles.active=test",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.boot.admin.client.enabled=false",
                "spring.main.lazy-initialization=true",
                "bacon.auth.repository.mode=memory",
                "bacon.order.repository.mode=memory",
                "bacon.inventory.repository.mode=memory",
                "bacon.payment.repository.mode=memory",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                        + "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
        })
class BaconMonoBootMonoAssemblyTest extends BaconSpringBootTest {

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
    void shouldWireLocalFacadeImplementationsInMonoMode() {
        assertThat(tokenVerifyFacade).isInstanceOf(TokenVerifyFacadeLocalImpl.class);
        assertThat(sessionCommandFacade).isInstanceOf(SessionCommandFacadeLocalImpl.class);
        assertThat(oAuthClientReadFacade).isInstanceOf(OAuthClientReadFacadeLocalImpl.class);
        assertThat(userReadFacade).isInstanceOf(UserReadFacadeLocalImpl.class);
        assertThat(departmentReadFacade).isInstanceOf(DepartmentReadFacadeLocalImpl.class);
        assertThat(roleReadFacade).isInstanceOf(RoleReadFacadeLocalImpl.class);
        assertThat(permissionReadFacade).isInstanceOf(PermissionReadFacadeLocalImpl.class);
        assertThat(orderReadFacade).isInstanceOf(OrderReadFacadeLocalImpl.class);
        assertThat(orderCommandFacade).isInstanceOf(OrderCommandFacadeLocalImpl.class);
        assertThat(inventoryReadFacade).isInstanceOf(InventoryReadFacadeLocalImpl.class);
        assertThat(inventoryCommandFacade).isInstanceOf(InventoryCommandFacadeLocalImpl.class);
        assertThat(paymentReadFacade).isInstanceOf(PaymentReadFacadeLocalImpl.class);
        assertThat(paymentCommandFacade).isInstanceOf(PaymentCommandFacadeLocalImpl.class);
        assertThat(currentUserProvider).isInstanceOf(MonoCurrentUserProvider.class);
    }
}
