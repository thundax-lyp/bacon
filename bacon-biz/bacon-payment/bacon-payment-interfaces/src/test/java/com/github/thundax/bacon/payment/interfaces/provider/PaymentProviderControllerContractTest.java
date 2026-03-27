package com.github.thundax.bacon.payment.interfaces.provider;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.service.PaymentApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentQueryApplicationService;
import jakarta.servlet.ServletException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentProviderControllerContractTest {

    @Test
    void shouldKeepRawProviderPayloadWithoutResponseEnvelope() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(new StubPaymentQueryApplicationService(),
                new PaymentApplicationService(null, null, null), new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/providers/payment/PAY-10001")
                        .param("tenantId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value("PAY-10001"))
                .andExpect(jsonPath("$.tenantId").value(1001))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenProviderRequiredParamMissing() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(new StubPaymentQueryApplicationService(),
                new PaymentApplicationService(null, null, null), new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/providers/payment/PAY-10001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExposeRawExceptionSemanticForProvider() {
        PaymentProviderController controller = new PaymentProviderController(new StubPaymentQueryApplicationService(),
                new PaymentApplicationService(null, null, null), new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                get("/providers/payment/PAY-10001")
                        .param("tenantId", "9999")));
        assertEquals("Invalid tenant: 9999", exception.getCause().getMessage());
    }

    private static final class StubPaymentQueryApplicationService extends PaymentQueryApplicationService {

        private StubPaymentQueryApplicationService() {
            super(null, null);
        }

        @Override
        public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
            if (Long.valueOf(9999L).equals(tenantId)) {
                throw new IllegalArgumentException("Invalid tenant: " + tenantId);
            }
            return new PaymentDetailDTO(tenantId, paymentNo, "ORD-10001", 2001L, "MOCK", "PAID",
                    new BigDecimal("88.80"), new BigDecimal("88.80"), Instant.parse("2026-03-27T10:00:00Z"),
                    Instant.parse("2026-03-27T10:30:00Z"), Instant.parse("2026-03-27T10:01:00Z"),
                    "provider-payment", null, "TXN-10001", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}");
        }
    }
}
