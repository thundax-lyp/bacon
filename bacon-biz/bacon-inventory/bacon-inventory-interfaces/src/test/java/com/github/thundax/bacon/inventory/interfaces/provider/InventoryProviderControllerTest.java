package com.github.thundax.bacon.inventory.interfaces.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.web.config.InternalApiGuardInterceptor;
import com.github.thundax.bacon.common.web.config.InternalApiGuardProperties;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryAvailableStockQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryBatchAvailableStockQuery;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import jakarta.servlet.ServletException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InventoryProviderControllerTest {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";
    private static final String PROVIDER_TOKEN = "inventory-token";

    @Test
    void shouldKeepRawProviderPayloadWithoutResponseEnvelope() throws Exception {
        InventoryProviderController controller = new InventoryProviderController(
                new StubInventoryQueryApplicationService(),
                new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/inventory/queries/available-stocks")
                        .param("skuIds", "101")
                        .param("skuIds", "102")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuId").value(101))
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldExposeRawExceptionWhenTenantContextMissing() {
        InventoryProviderController controller = new InventoryProviderController(
                new StubInventoryQueryApplicationService(),
                new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        BaconContextHolder.clear();

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get("/providers/inventory/queries/available-stocks")
                        .param("skuIds", "101")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN)));
        assertEquals("tenantId must not be null", exception.getCause().getMessage());
    }

    @Test
    void shouldExposeRawExceptionSemanticForProvider() {
        InventoryProviderController controller = new InventoryProviderController(
                new StubInventoryQueryApplicationService(),
                new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        BaconContextHolder.set(new BaconContext(9999L, 2001L));

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get("/providers/inventory/queries/available-stocks")
                        .param("skuIds", "101")
                        .header(PROVIDER_TOKEN_HEADER, PROVIDER_TOKEN)));
        assertEquals("Invalid tenant: 9999", exception.getCause().getMessage());
    }

    @Test
    void shouldRejectProviderCallWhenTokenMissing() throws Exception {
        InventoryProviderController controller = new InventoryProviderController(
                new StubInventoryQueryApplicationService(),
                new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/inventory/queries/available-stocks").param("skuIds", "101"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProviderCallWhenTokenInvalid() throws Exception {
        InventoryProviderController controller = new InventoryProviderController(
                new StubInventoryQueryApplicationService(),
                new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(providerGuardInterceptor())
                .build();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));

        mockMvc.perform(get("/providers/inventory/queries/available-stocks")
                        .param("skuIds", "101")
                        .header(PROVIDER_TOKEN_HEADER, "wrong-token"))
                .andExpect(status().isForbidden());
    }

    private InternalApiGuardInterceptor providerGuardInterceptor() {
        InternalApiGuardProperties guardProperties = new InternalApiGuardProperties();
        guardProperties.setEnabled(true);
        guardProperties.setHeaderName(PROVIDER_TOKEN_HEADER);
        guardProperties.setToken(PROVIDER_TOKEN);
        guardProperties.setIncludePathPatterns(List.of("/providers/inventory/**"));
        return new InternalApiGuardInterceptor(guardProperties);
    }

    private static final class StubInventoryQueryApplicationService extends InventoryQueryApplicationService {

        private StubInventoryQueryApplicationService() {
            super(null, null, null, null);
        }

        @Override
        public List<InventoryStockDTO> batchGetAvailableStock(InventoryBatchAvailableStockQuery query) {
            Long tenantIdValue = BaconContextHolder.currentTenantId();
            if (tenantIdValue == null) {
                throw new IllegalArgumentException("tenantId must not be null");
            }
            if (Long.valueOf(9999L).equals(tenantIdValue)) {
                throw new IllegalArgumentException("Invalid tenant: " + tenantIdValue);
            }
            return List.of(new InventoryStockDTO(
                    101L, "DEFAULT", 100, 0, 100, "ENABLED", Instant.parse("2026-03-26T10:00:00Z")));
        }

        @Override
        public InventoryStockDTO getAvailableStock(InventoryAvailableStockQuery query) {
            Long tenantIdValue = BaconContextHolder.currentTenantId();
            if (tenantIdValue == null) {
                throw new IllegalArgumentException("tenantId must not be null");
            }
            if (Long.valueOf(9999L).equals(tenantIdValue)) {
                throw new IllegalArgumentException("Invalid tenant: " + tenantIdValue);
            }
            return new InventoryStockDTO(
                    101L, "DEFAULT", 100, 0, 100, "ENABLED", Instant.parse("2026-03-26T10:00:00Z"));
        }
    }
}
