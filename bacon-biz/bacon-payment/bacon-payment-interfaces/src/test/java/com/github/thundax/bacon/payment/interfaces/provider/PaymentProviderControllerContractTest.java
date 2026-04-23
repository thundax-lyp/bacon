package com.github.thundax.bacon.payment.interfaces.provider;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.payment.application.audit.PaymentAuditQueryApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseResult;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateResult;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentProviderControllerContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "payment-token";

    @Mock
    private PaymentQueryApplicationService paymentQueryApplicationService;

    @Mock
    private PaymentAuditQueryApplicationService paymentAuditQueryApplicationService;

    @Mock
    private PaymentCreateApplicationService paymentCreateApplicationService;

    @Mock
    private PaymentCloseApplicationService paymentCloseApplicationService;

    @Test
    void shouldExposeReadProviderContracts() throws Exception {
        when(paymentQueryApplicationService.getByPaymentNo(argThat(
                        query -> query != null && query.paymentNo().equals("PAY-1"))))
                .thenReturn(detailDto());
        when(paymentQueryApplicationService.getByOrderNo(argThat(
                        query -> query != null && query.orderNo().equals("ORD-1"))))
                .thenReturn(detailDto());
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(get("/providers/payment/queries/detail")
                        .param("paymentNo", "PAY-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value("PAY-1"))
                .andExpect(jsonPath("$.callbackSummary").value("{\"tradeStatus\":\"SUCCESS\"}"))
                .andExpect(jsonPath("$.code").doesNotExist());
        mockMvc.perform(get("/providers/payment/queries/by-order")
                        .param("orderNo", "ORD-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value("PAY-1"))
                .andExpect(jsonPath("$.orderNo").value("ORD-1"));
    }

    @Test
    void shouldExposeCommandProviderContracts() throws Exception {
        when(paymentCreateApplicationService.createPayment(argThat(command -> command != null
                        && command.orderNo().equals("ORD-1")
                        && command.userId().equals(2001L)
                        && command.amount().compareTo(BigDecimal.TEN) == 0
                        && command.channelCode().equals("MOCK")
                        && command.subject().equals("test-subject")
                        && command.expiredAt().equals(Instant.parse("2026-03-27T10:30:00Z")))))
                .thenReturn(new PaymentCreateResult(
                        "PAY-1",
                        "ORD-1",
                        "MOCK",
                        "PAYING",
                        "mock://pay/PAY-1",
                        Instant.parse("2026-03-27T10:30:00Z"),
                        null));
        when(paymentCloseApplicationService.closePayment(argThat(command -> command != null
                        && command.paymentNo().equals("PAY-1")
                        && command.reason().equals("SYSTEM_CANCELLED"))))
                .thenReturn(new PaymentCloseResult("PAY-1", "ORD-1", "CLOSED", "SUCCESS", "SYSTEM_CANCELLED", null));
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(post("/providers/payment/commands/create")
                        .param("orderNo", "ORD-1")
                        .param("userId", "2001")
                        .param("amount", "10")
                        .param("channelCode", "MOCK")
                        .param("subject", "test-subject")
                        .param("expiredAt", "2026-03-27T10:30:00Z")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value("PAY-1"))
                .andExpect(jsonPath("$.payPayload").value("mock://pay/PAY-1"));
        mockMvc.perform(post("/providers/payment/commands/close")
                        .param("paymentNo", "PAY-1")
                        .param("reason", "SYSTEM_CANCELLED")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closeResult").value("SUCCESS"))
                .andExpect(jsonPath("$.closeReason").value("SYSTEM_CANCELLED"));

        verify(paymentCreateApplicationService).createPayment(argThat(command -> command.orderNo().equals("ORD-1")));
        verify(paymentCloseApplicationService).closePayment(argThat(command -> command.paymentNo().equals("PAY-1")));
    }

    private MockMvc newMockMvc() {
        PaymentProviderController controller = new PaymentProviderController(
                paymentQueryApplicationService,
                paymentAuditQueryApplicationService,
                paymentCreateApplicationService,
                paymentCloseApplicationService);
        return MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/payment/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private PaymentDetailDTO detailDto() {
        return new PaymentDetailDTO(
                "PAY-1",
                "ORD-1",
                2001L,
                "MOCK",
                "PAID",
                BigDecimal.TEN,
                BigDecimal.TEN,
                Instant.parse("2026-03-27T10:00:00Z"),
                Instant.parse("2026-03-27T10:30:00Z"),
                Instant.parse("2026-03-27T10:01:00Z"),
                "test-subject",
                null,
                "TXN-1",
                "SUCCESS",
                "{\"tradeStatus\":\"SUCCESS\"}");
    }
}
