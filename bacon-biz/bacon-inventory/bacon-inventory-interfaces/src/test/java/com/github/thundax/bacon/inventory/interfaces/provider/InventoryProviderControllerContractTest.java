package com.github.thundax.bacon.inventory.interfaces.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class InventoryProviderControllerContractTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "inventory-token";

    @Mock
    private InventoryQueryApplicationService inventoryQueryApplicationService;

    @Mock
    private InventoryCommandApplicationService inventoryCommandApplicationService;

    @Test
    void shouldExposeReadProviderContracts() throws Exception {
        when(inventoryQueryApplicationService.getAvailableStock(
                        argThat(query -> query != null && query.skuId().value().equals(101L))))
                .thenReturn(stockDto(101L));
        when(inventoryQueryApplicationService.batchGetAvailableStock(argThat(
                        query -> query != null && query.skuIds().stream().anyMatch(skuId -> skuId.value().equals(102L)))))
                .thenReturn(List.of(stockDto(101L), stockDto(102L)));
        when(inventoryQueryApplicationService.getReservationByOrderNo(argThat(
                        query -> query != null && query.orderNo().value().equals("ORD-1"))))
                .thenReturn(reservationDto());
        MockMvc mockMvc = newMockMvc();

        mockMvc.perform(get("/providers/inventory/queries/available-stock")
                        .param("skuId", "101")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuId").value(101))
                .andExpect(jsonPath("$.availableQuantity").value(80));
        mockMvc.perform(get("/providers/inventory/queries/available-stocks")
                        .param("skuIds", "101")
                        .param("skuIds", "102")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuId").value(101))
                .andExpect(jsonPath("$.records").doesNotExist());
        mockMvc.perform(get("/providers/inventory/queries/reservation")
                        .param("orderNo", "ORD-1")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD-1"))
                .andExpect(jsonPath("$.inventoryStatus").value("RESERVED"));
    }

    @Test
    void shouldExposeCommandProviderContracts() throws Exception {
        when(inventoryCommandApplicationService.reserveStock(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.items().get(0).skuId().value().equals(101L))))
                .thenReturn(reservedResult());
        when(inventoryCommandApplicationService.releaseReservedStock(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.reason().name().equals("TIMEOUT_CLOSED"))))
                .thenReturn(releasedResult());
        when(inventoryCommandApplicationService.deductReservedStock(argThat(
                        command -> command != null && command.orderNo().value().equals("ORD-1"))))
                .thenReturn(deductedResult());
        MockMvc mockMvc = newMockMvc();

        MvcResult reserveResult = mockMvc.perform(post("/providers/inventory/commands/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "orderNo": "ORD-1",
                                  "items": [
                                    {"skuId": 101, "quantity": 2}
                                  ]
                                }
                                """)
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryStatus").value("RESERVED"))
                .andReturn();
        mockMvc.perform(post("/providers/inventory/commands/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-1\",\"reason\":\"TIMEOUT_CLOSED\"}")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryStatus").value("RELEASED"))
                .andExpect(jsonPath("$.releaseReason").value("TIMEOUT_CLOSED"));
        mockMvc.perform(post("/providers/inventory/commands/deduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-1\"}")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryStatus").value("DEDUCTED"));

        assertThat(reserveResult.getResponse().getContentAsString()).contains("\"items\":[]");
        verify(inventoryCommandApplicationService).deductReservedStock(argThat(command -> command.orderNo().value().equals("ORD-1")));
    }

    private MockMvc newMockMvc() {
        InventoryProviderController controller =
                new InventoryProviderController(inventoryQueryApplicationService, inventoryCommandApplicationService);
        return MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/inventory/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private InventoryStockDTO stockDto(Long skuId) {
        return new InventoryStockDTO(
                skuId, "DEFAULT", 100, 20, 80, "ENABLED", Instant.parse("2026-03-26T10:00:00Z"));
    }

    private InventoryReservationDTO reservationDto() {
        return new InventoryReservationDTO(
                "ORD-1",
                "RSV-1",
                "RESERVED",
                "DEFAULT",
                List.of(new InventoryReservationItemDTO(101L, 2)),
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                null,
                null);
    }

    private InventoryReservationResult reservedResult() {
        return new InventoryReservationResult("ORD-1", "RSV-1", "RESERVED", "RESERVED", "DEFAULT", null, null, null, null);
    }

    private InventoryReservationResult releasedResult() {
        return new InventoryReservationResult(
                "ORD-1",
                "RSV-1",
                "RELEASED",
                "RELEASED",
                "DEFAULT",
                null,
                "TIMEOUT_CLOSED",
                Instant.parse("2026-03-26T10:30:00Z"),
                null);
    }

    private InventoryReservationResult deductedResult() {
        return new InventoryReservationResult(
                "ORD-1",
                "RSV-1",
                "DEDUCTED",
                "DEDUCTED",
                "DEFAULT",
                null,
                null,
                null,
                Instant.parse("2026-03-26T10:40:00Z"));
    }
}
