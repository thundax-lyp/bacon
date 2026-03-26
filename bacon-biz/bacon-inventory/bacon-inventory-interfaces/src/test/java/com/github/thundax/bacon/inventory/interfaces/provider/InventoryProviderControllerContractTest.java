package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import jakarta.servlet.ServletException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryProviderControllerContractTest {

    @Test
    void shouldKeepRawProviderPayloadWithoutResponseEnvelope() throws Exception {
        InventoryProviderController controller = new InventoryProviderController(new StubInventoryQueryApplicationService(),
                new InventoryApplicationService(null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/providers/inventory/stocks")
                        .param("tenantId", "1001")
                        .param("skuIds", "101")
                        .param("skuIds", "102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuId").value(101))
                .andExpect(jsonPath("$[0].tenantId").value(1001))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenProviderRequiredParamMissing() throws Exception {
        InventoryProviderController controller = new InventoryProviderController(new StubInventoryQueryApplicationService(),
                new InventoryApplicationService(null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/providers/inventory/stocks")
                        .param("skuIds", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExposeRawExceptionSemanticForProvider() {
        InventoryProviderController controller = new InventoryProviderController(new StubInventoryQueryApplicationService(),
                new InventoryApplicationService(null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                get("/providers/inventory/stocks")
                        .param("tenantId", "9999")
                        .param("skuIds", "101")));
        assertEquals("Invalid tenant: 9999", exception.getCause().getMessage());
    }

    private static final class StubInventoryQueryApplicationService extends InventoryQueryApplicationService {

        private StubInventoryQueryApplicationService() {
            super(null, null, null, null);
        }

        @Override
        public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
            if (Long.valueOf(9999L).equals(tenantId)) {
                throw new IllegalArgumentException("Invalid tenant: " + tenantId);
            }
            return List.of(new InventoryStockDTO(tenantId, 101L, 1L,
                    100, 0, 100, "ENABLED", Instant.parse("2026-03-26T10:00:00Z")));
        }
    }
}
