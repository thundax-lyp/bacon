package com.github.thundax.bacon.order.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.command.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.interfaces.provider.OrderReadProviderController;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderInterfaceContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "order-token";

    @Test
    void wrappedControllerShouldReturnUnifiedSuccessEnvelope() throws Exception {
        OrderController controller = new OrderController(null, new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .build();

        mockMvc.perform(get("/api/orders")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.records[0].orderNo").value("ORD-1"));
    }

    @Test
    void wrappedControllerShouldReturnBadRequestForIllegalArgument() throws Exception {
        OrderController controller = new OrderController(null, new StubOrderQueryApplicationService(),
                new OrderCancelApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .build();

        mockMvc.perform(get("/api/orders")
                        .param("tenantId", "9999")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void providerControllerShouldKeepRawDtoContract() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(
                new StubOrderQueryApplicationService(),
                new OrderPaymentResultApplicationService(null, null, null, null),
                new OrderTimeoutApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();

        mockMvc.perform(get("/providers/orders")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "20")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].orderNo").value("ORD-1"))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void providerControllerShouldExposeRawExceptionSemantic() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(
                new StubOrderQueryApplicationService(),
                new OrderPaymentResultApplicationService(null, null, null, null),
                new OrderTimeoutApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(get("/providers/orders")
                .param("tenantId", "9999")
                .param("pageNo", "1")
                .param("pageSize", "20")
                .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN)));
        assertEquals("Invalid tenant: 9999", exception.getCause().getMessage());
    }

    @Test
    void providerControllerShouldRejectMissingToken() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(
                new StubOrderQueryApplicationService(),
                new OrderPaymentResultApplicationService(null, null, null, null),
                new OrderTimeoutApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();

        mockMvc.perform(get("/providers/orders")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void providerControllerShouldRejectWrongToken() throws Exception {
        OrderReadProviderController controller = new OrderReadProviderController(
                new StubOrderQueryApplicationService(),
                new OrderPaymentResultApplicationService(null, null, null, null),
                new OrderTimeoutApplicationService(null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();

        mockMvc.perform(get("/providers/orders")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "20")
                        .header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/orders/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private static final class StubOrderQueryApplicationService extends OrderQueryApplicationService {

        private StubOrderQueryApplicationService() {
            super(null);
        }

        @Override
        public OrderDetailDTO getById(Long tenantId, Long orderId) {
            if (!Long.valueOf(1001L).equals(tenantId)) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            return new OrderDetailDTO(orderId, String.valueOf(tenantId), "ORD-1", "2001",
                    "CREATED", "UNPAID", "UNRESERVED", null, null,
                    "CNY", BigDecimal.TEN, BigDecimal.TEN, null, null,
                    Instant.parse("2026-03-26T10:00:00Z"), Instant.parse("2026-03-26T10:30:00Z"),
                    List.of(), null, null, null, null);
        }

        @Override
        public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
            if ("9999".equals(query.getTenantId())) {
                throw new IllegalArgumentException("Invalid tenant: " + query.getTenantId());
            }
            OrderSummaryDTO summary = new OrderSummaryDTO("1", "1001", "ORD-1", "2001",
                    "CREATED", "UNPAID", "UNRESERVED", null, null,
                    "CNY", BigDecimal.TEN, BigDecimal.TEN, null, null,
                    Instant.parse("2026-03-26T10:00:00Z"), Instant.parse("2026-03-26T10:30:00Z"));
            return new OrderPageResultDTO(List.of(summary), 1, 1, 20);
        }
    }
}
