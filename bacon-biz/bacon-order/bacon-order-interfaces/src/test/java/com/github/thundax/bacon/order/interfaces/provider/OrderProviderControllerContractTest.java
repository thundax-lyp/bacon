package com.github.thundax.bacon.order.interfaces.provider;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderProviderControllerContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "order-token";

    @Mock
    private OrderPaymentResultApplicationService orderPaymentResultApplicationService;

    @Mock
    private OrderTimeoutApplicationService orderTimeoutApplicationService;

    @Test
    void shouldExposePaymentResultCommandContracts() throws Exception {
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(post("/providers/order/commands/mark-paid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "orderNo": "ORD-1",
                                  "paymentNo": "PAY-1",
                                  "channelCode": "WECHAT",
                                  "paidAmount": 10,
                                  "paidTime": "2026-03-26T10:05:00Z"
                                }
                                """)
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(post("/providers/order/commands/mark-payment-failed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "orderNo": "ORD-2",
                                  "paymentNo": "PAY-2",
                                  "reason": "insufficient balance",
                                  "channelStatus": "FAILED",
                                  "failedTime": "2026-03-26T10:06:00Z"
                                }
                                """)
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());

        verify(orderPaymentResultApplicationService)
                .markPaid(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.paymentNo().value().equals("PAY-1")
                        && command.channelCode().equals("WECHAT")
                        && command.paidAmount().compareTo(BigDecimal.TEN) == 0
                        && command.paidTime().equals(Instant.parse("2026-03-26T10:05:00Z"))));
        verify(orderPaymentResultApplicationService)
                .markPaymentFailed(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-2")
                        && command.paymentNo().value().equals("PAY-2")
                        && command.reason().equals("insufficient balance")
                        && command.channelStatus().equals("FAILED")
                        && command.failedTime().equals(Instant.parse("2026-03-26T10:06:00Z"))));
    }

    @Test
    void shouldExposeCloseExpiredCommandContract() throws Exception {
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(post("/providers/order/commands/close-expired")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "orderNo": "ORD-1",
                                  "reason": "expired"
                                }
                                """)
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());

        verify(orderTimeoutApplicationService)
                .closeExpiredOrder(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.reason().equals("expired")));
    }

    private MockMvc newMockMvc() {
        OrderCommandProviderController controller =
                new OrderCommandProviderController(orderPaymentResultApplicationService, orderTimeoutApplicationService);
        return MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/order/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }
}
