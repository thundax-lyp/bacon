package com.github.thundax.bacon.payment.interfaces.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class PaymentQueryControllerContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentQueryController controller = new PaymentQueryController(new StubPaymentQueryApplicationService());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnWrappedResponseWhenRequestIsValid() throws Exception {
        mockMvc.perform(get("/payments/PAY-10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentNo").value("PAY-10001"))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-10001"));
    }

    @Test
    void shouldExposePaymentErrorCodeWhenBusinessExceptionOccurs() throws Exception {
        mockMvc.perform(get("/payments/PAY-40401"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(PaymentErrorCode.PAYMENT_NOT_FOUND.code()));
    }

    private static final class StubPaymentQueryApplicationService extends PaymentQueryApplicationService {

        private StubPaymentQueryApplicationService() {
            super(null, null);
        }

        @Override
        public PaymentDetailDTO getByPaymentNo(String paymentNo) {
            if ("PAY-40401".equals(paymentNo)) {
                throw new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo);
            }
            return new PaymentDetailDTO(
                    paymentNo,
                    "ORD-10001",
                    2001L,
                    "MOCK",
                    "PAID",
                    new BigDecimal("88.80"),
                    new BigDecimal("88.80"),
                    Instant.parse("2026-03-27T10:00:00Z"),
                    Instant.parse("2026-03-27T10:30:00Z"),
                    Instant.parse("2026-03-27T10:01:00Z"),
                    "test-payment",
                    null,
                    "TXN-10001",
                    "SUCCESS",
                    "{\"tradeStatus\":\"SUCCESS\"}");
        }
    }
}
