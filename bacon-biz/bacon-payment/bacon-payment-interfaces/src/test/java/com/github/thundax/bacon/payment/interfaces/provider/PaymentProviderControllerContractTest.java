package com.github.thundax.bacon.payment.interfaces.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.common.web.resolver.CurrentTenantArgumentResolver;
import com.github.thundax.bacon.payment.api.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.audit.PaymentAuditQueryApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import jakarta.servlet.ServletException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PaymentProviderControllerContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "payment-token";

    @Test
    void shouldKeepRawProviderPayloadWithoutResponseEnvelope() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/payment/PAY-10001").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentNo").value("PAY-10001"))
                .andExpect(jsonPath("$.tenantId").value(1001))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenProviderRequiredParamMissing() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.clear();

        mockMvc.perform(get("/providers/payment/PAY-10001").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExposeRawExceptionSemanticForProvider() {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(9999L, 2001L));

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(
                        get("/providers/payment/PAY-10001").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN)));
        assertEquals(PaymentDomainException.class, exception.getCause().getClass());
        assertEquals(
                PaymentErrorCode.PAYMENT_NOT_FOUND.code(), ((PaymentDomainException) exception.getCause()).getCode());
    }

    @Test
    void shouldExposeRawAuditLogPayloadForProvider() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/payment/PAY-10001/audit-logs").header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentNo").value("PAY-10001"))
                .andExpect(jsonPath("$[0].actionType").value("CREATE"));
    }

    @Test
    void shouldRejectProviderCallWhenTokenMissing() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/payment/PAY-10001")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProviderCallWhenTokenInvalid() throws Exception {
        PaymentProviderController controller = new PaymentProviderController(
                new StubPaymentQueryApplicationService(),
                new StubPaymentAuditQueryApplicationService(),
                new PaymentCreateApplicationService(null, null, null),
                new PaymentCloseApplicationService(null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/payment/PAY-10001").header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/payment/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private static final class StubPaymentQueryApplicationService extends PaymentQueryApplicationService {

        private StubPaymentQueryApplicationService() {
            super(null, null);
        }

        @Override
        public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
            if (Long.valueOf(9999L).equals(tenantId)) {
                throw new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo);
            }
            return new PaymentDetailDTO(
                    tenantId,
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
                    "provider-payment",
                    null,
                    "TXN-10001",
                    "SUCCESS",
                    "{\"tradeStatus\":\"SUCCESS\"}");
        }
    }

    private static final class StubPaymentAuditQueryApplicationService extends PaymentAuditQueryApplicationService {

        private StubPaymentAuditQueryApplicationService() {
            super(null);
        }

        @Override
        public List<PaymentAuditLogDTO> getByPaymentNo(Long tenantId, String paymentNo) {
            return List.of(new PaymentAuditLogDTO(
                    tenantId,
                    paymentNo,
                    "CREATE",
                    null,
                    "PAYING",
                    "SYSTEM",
                    "0",
                    Instant.parse("2026-03-27T10:00:00Z")));
        }
    }
}
