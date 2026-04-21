package com.github.thundax.bacon.order.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.common.web.resolver.CurrentTenantArgumentResolver;
import com.github.thundax.bacon.order.application.command.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.command.OrderCloseExpiredCommand;
import com.github.thundax.bacon.order.application.command.OrderOutboxDeadLetterReplayApplicationService;
import com.github.thundax.bacon.order.application.command.OrderMarkPaidCommand;
import com.github.thundax.bacon.order.application.command.OrderMarkPaymentFailedCommand;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.query.OrderByOrderNoQuery;
import com.github.thundax.bacon.order.application.query.OrderPageQuery;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.result.OrderOutboxDeadLetterReplayResult;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.interfaces.provider.OrderCommandProviderController;
import com.github.thundax.bacon.order.interfaces.provider.OrderReadProviderController;
import jakarta.servlet.ServletException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderInterfaceTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "order-token";

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldReturnUnifiedSuccessEnvelope() throws Exception {
        OrderController controller = new OrderController(
                null,
                new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null),
                new StubOrderOutboxDeadLetterReplayApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/order").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("ORD-1"));
    }

    @Test
    void shouldReturnBadRequestForIllegalArgument() throws Exception {
        OrderController controller = new OrderController(
                null,
                new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null),
                new StubOrderOutboxDeadLetterReplayApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(9999L, 2001L));

        mockMvc.perform(get("/order").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void shouldReturnBadRequestForIllegalEnumFilter() throws Exception {
        OrderController controller = new OrderController(
                null,
                new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null),
                new StubOrderOutboxDeadLetterReplayApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/order")
                        .param("orderStatus", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Unsupported order status: INVALID"));
    }

    @Test
    void shouldExposeDeadLetterReplayEndpoint() throws Exception {
        OrderController controller = new OrderController(
                null,
                new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null),
                new StubOrderOutboxDeadLetterReplayApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/order/outbox/dead-letters/1001/replay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.deadLetterId").value(1001L))
                .andExpect(jsonPath("$.data.replayStatus").value("SUCCESS"));
    }

    @Test
    void shouldKeepRawDtoContractWithoutResponseEnvelope() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(new StubOrderQueryApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/order")
                        .param("pageNo", "1")
                        .param("pageSize", "20")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].orderNo").value("ORD-1"))
                .andExpect(jsonPath("$.records[0].id").doesNotExist())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldUseFacadeRequestContract() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(new StubOrderQueryApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/order/ORD-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD-1"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void shouldExposeRawExceptionSemantic() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(new StubOrderQueryApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(9999L, 2001L));

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get("/providers/order")
                        .param("pageNo", "1")
                        .param("pageSize", "20")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN)));
        assertEquals("Invalid tenant: 9999", exception.getCause().getMessage());
    }

    @Test
    void shouldRejectMissingToken() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(new StubOrderQueryApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/order").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWrongToken() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(new StubOrderQueryApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/order")
                        .param("pageNo", "1")
                        .param("pageSize", "20")
                        .header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldExposeWriteEndpoints() throws Exception {
        OrderCommandProviderController controller = new OrderCommandProviderController(
                new StubOrderPaymentResultApplicationService(), new StubOrderTimeoutApplicationService());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/providers/order/close-expired")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-1\",\"reason\":\"expired\"}")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk());
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/order/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private static final class StubOrderQueryApplicationService extends OrderQueryApplicationService {

        private StubOrderQueryApplicationService() {
            super(null, null, null, null, null);
        }

        @Override
        public OrderDetailDTO getById(OrderId orderId) {
            return new OrderDetailDTO(
                    orderId == null ? null : orderId.value(),
                    "ORD-1",
                    2001L,
                    "CREATED",
                    "UNPAID",
                    "UNRESERVED",
                    null,
                    null,
                    "CNY",
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    null,
                    null,
                    Instant.parse("2026-03-26T10:00:00Z"),
                    Instant.parse("2026-03-26T10:30:00Z"),
                    List.of(),
                    null,
                    null,
                    null,
                    null);
        }

        @Override
        public OrderDetailDTO getByOrderNo(OrderByOrderNoQuery query) {
            return new OrderDetailDTO(
                    1L,
                    query.orderNo() == null ? null : query.orderNo().value(),
                    2001L,
                    "CREATED",
                    "UNPAID",
                    "UNRESERVED",
                    null,
                    null,
                    "CNY",
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    null,
                    null,
                    Instant.parse("2026-03-26T10:00:00Z"),
                    Instant.parse("2026-03-26T10:30:00Z"),
                    List.of(),
                    null,
                    null,
                    null,
                    null);
        }

        @Override
        public OrderPageResult page(OrderPageQuery query) {
            if (Long.valueOf(9999L)
                    .equals(com.github.thundax.bacon.common.core.context.BaconContextHolder.currentTenantId())) {
                throw new IllegalArgumentException("Invalid tenant: "
                        + com.github.thundax.bacon.common.core.context.BaconContextHolder.currentTenantId());
            }
            OrderSummaryDTO summary = new OrderSummaryDTO(
                    1L,
                    "ORD-1",
                    2001L,
                    "CREATED",
                    "UNPAID",
                    "UNRESERVED",
                    null,
                    null,
                    "CNY",
                    BigDecimal.TEN,
                    BigDecimal.TEN,
                    null,
                    null,
                    Instant.parse("2026-03-26T10:00:00Z"),
                    Instant.parse("2026-03-26T10:30:00Z"));
            return new OrderPageResult(List.of(summary), 1, 1, 20);
        }
    }

    private static final class StubOrderOutboxDeadLetterReplayApplicationService
            extends OrderOutboxDeadLetterReplayApplicationService {

        private StubOrderOutboxDeadLetterReplayApplicationService() {
            super(null, null);
        }

        @Override
        public OrderOutboxDeadLetterReplayResult replay(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId deadLetterId) {
            return new OrderOutboxDeadLetterReplayResult(
                    deadLetterId == null ? null : deadLetterId.value(), "SUCCESS", "ok");
        }
    }

    private static final class StubOrderPaymentResultApplicationService extends OrderPaymentResultApplicationService {

        private StubOrderPaymentResultApplicationService() {
            super(null, null, null, null);
        }

        @Override
        public void markPaid(OrderMarkPaidCommand command) {}

        @Override
        public void markPaymentFailed(OrderMarkPaymentFailedCommand command) {}
    }

    private static final class StubOrderTimeoutApplicationService extends OrderTimeoutApplicationService {

        private StubOrderTimeoutApplicationService() {
            super(null, null, null, null, null);
        }

        @Override
        public void closeExpiredOrder(OrderCloseExpiredCommand command) {}
    }
}
