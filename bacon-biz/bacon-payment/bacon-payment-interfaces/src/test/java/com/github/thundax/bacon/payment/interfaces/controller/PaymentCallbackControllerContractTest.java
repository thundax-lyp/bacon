package com.github.thundax.bacon.payment.interfaces.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.payment.application.command.PaymentCallbackApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class PaymentCallbackControllerContractTest {

    @Test
    void shouldRejectSuccessfulCallbackWithoutTransactionNo() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(
                        post("/payment/callback/MOCK")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "paymentNo": "PAY-10001",
                                  "success": true,
                                  "channelStatus": "SUCCESS",
                                  "rawPayload": "{\\"tradeStatus\\":\\"SUCCESS\\"}"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFailedCallbackWithoutReason() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(
                        post("/payment/callback/MOCK")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "paymentNo": "PAY-10001",
                                  "success": false,
                                  "channelStatus": "FAILED",
                                  "rawPayload": "{\\"tradeStatus\\":\\"FAILED\\"}"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptValidSuccessfulCallback() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(
                        post("/payment/callback/MOCK")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "paymentNo": "PAY-10001",
                                  "success": true,
                                  "channelTransactionNo": "TXN-10001",
                                  "channelStatus": "SUCCESS",
                                  "rawPayload": "{\\"tradeStatus\\":\\"SUCCESS\\"}"
                                }
                                """))
                .andExpect(status().isOk());
    }

    private MockMvc buildMockMvc() {
        PaymentCallbackController controller =
                new PaymentCallbackController(new StubPaymentCallbackApplicationService());
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    private static final class StubPaymentCallbackApplicationService extends PaymentCallbackApplicationService {

        private StubPaymentCallbackApplicationService() {
            super(null, null, null, null, null);
        }

        @Override
        public void callbackPaid(
                String channelCode,
                String paymentNo,
                String channelTransactionNo,
                String channelStatus,
                String rawPayload) {}

        @Override
        public void callbackFailed(
                String channelCode, String paymentNo, String channelStatus, String rawPayload, String reason) {}
    }
}
