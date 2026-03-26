package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryService;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryControllerContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        StubInventoryRepository repository = new StubInventoryRepository();
        InventoryQueryService inventoryQueryService = new InventoryQueryService(repository, repository, repository);
        InventoryController controller = new InventoryController(null, inventoryQueryService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldRejectPageRequestWithoutTenantId() throws Exception {
        mockMvc.perform(get("/inventories/page")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPageRequestWithOversizedPageSize() throws Exception {
        mockMvc.perform(get("/inventories/page")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "1000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPagedInventoryWhenRequestIsValid() throws Exception {
        mockMvc.perform(get("/inventories/page")
                        .param("tenantId", "1001")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNo").value(1))
                .andExpect(jsonPath("$.records[0].skuId").value(101));
    }

    private static final class StubInventoryRepository implements InventoryStockRepository,
            InventoryReservationRepository, InventoryLogRepository {

        private final Inventory stock = new Inventory(1L, 1001L, 101L, 1L, 100,
                10, 90, Inventory.STATUS_ENABLED, 1L, Instant.parse("2026-03-26T10:00:00Z"));

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
            return Optional.of(stock);
        }

        @Override
        public List<Inventory> findInventories(Long tenantId) {
            return List.of(stock);
        }

        @Override
        public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
            return List.of(stock);
        }

        @Override
        public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
            return List.of(stock);
        }

        @Override
        public long countInventories(Long tenantId, Long skuId, String status) {
            return 1;
        }

        @Override
        public Inventory saveInventory(Inventory inventory) {
            return inventory;
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
            return Optional.empty();
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.of();
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.of();
        }

        @Override
        public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        }

        @Override
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
        }
    }
}
