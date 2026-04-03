package com.github.thundax.bacon.payment.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.payment.api.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.application.audit.PaymentAuditQueryApplicationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentAuditLogControllerContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentAuditLogController controller = new PaymentAuditLogController(new StubPaymentAuditQueryApplicationService());
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnWrappedAuditLogs() throws Exception {
        mockMvc.perform(get("/payments/PAY-10001/audit-logs")
                        .param("tenantId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].paymentNo").value("PAY-10001"))
                .andExpect(jsonPath("$.data[0].actionType").value("CREATE"));
    }

    @Test
    void shouldRejectAuditLogQueryWhenTenantIdMissing() throws Exception {
        mockMvc.perform(get("/payments/PAY-10001/audit-logs"))
                .andExpect(status().isBadRequest());
    }

    private static final class StubPaymentAuditQueryApplicationService extends PaymentAuditQueryApplicationService {

        private StubPaymentAuditQueryApplicationService() {
            super(null);
        }

        @Override
        public List<PaymentAuditLogDTO> getByPaymentNo(Long tenantId, String paymentNo) {
            return List.of(new PaymentAuditLogDTO(String.valueOf(tenantId), paymentNo, "CREATE", null, "PAYING",
                    "SYSTEM", "0", Instant.parse("2026-03-27T10:00:00Z")));
        }
    }
}
