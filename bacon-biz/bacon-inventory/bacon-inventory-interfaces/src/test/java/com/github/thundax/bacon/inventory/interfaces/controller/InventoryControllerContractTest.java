package com.github.thundax.bacon.inventory.interfaces.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.web.advice.ApiResponseBodyAdvice;
import com.github.thundax.bacon.common.web.advice.GlobalExceptionHandler;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class InventoryControllerContractTest {

    private MockMvc mockMvc;
    private InventoryController controller;
    private LocalValidatorFactoryBean validator;
    private StubInventoryRepository repository;

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        repository = new StubInventoryRepository();
        InventoryQueryApplicationService inventoryQueryService =
                new InventoryQueryApplicationService(repository, repository, repository, repository);
        controller = new InventoryController(
                new InventoryCommandApplicationService(null, null, null, null, null, null, null), inventoryQueryService);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldAllowPageRequestWithoutTenantIdParam() throws Exception {
        mockMvc.perform(get("/inventory/stocks/page").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNo").value(1))
                .andExpect(jsonPath("$.records[0].skuId").value(101));
    }

    @Test
    void shouldRejectPageRequestWithOversizedPageSize() throws Exception {
        mockMvc.perform(get("/inventory/stocks/page").param("pageNo", "1").param("pageSize", "1000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPagedInventoryWhenRequestIsValid() throws Exception {
        mockMvc.perform(get("/inventory/stocks/page")
                        .param("status", "ENABLED")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNo").value(1))
                .andExpect(jsonPath("$.records[0].skuId").value(101));
    }

    @Test
    void shouldRejectPageRequestWithIllegalStatus() throws Exception {
        MockMvc wrappedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice(new ObjectMapper()))
                .setValidator(validator)
                .build();

        wrappedMockMvc
                .perform(get("/inventory/stocks/page")
                        .param("status", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Unknown inventory status: INVALID"));
    }

    @Test
    void shouldExposeTenantAndUserContextToRepositoryLayer() throws Exception {
        mockMvc.perform(get("/inventory/stocks/page").param("pageNo", "1").param("pageSize", "20"))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(repository.capturedTenantId).isEqualTo(1001L);
        org.assertj.core.api.Assertions.assertThat(repository.capturedUserId).isEqualTo(2001L);
    }

    private static final class StubInventoryRepository
            implements InventoryStockRepository,
                    InventoryReservationRepository,
                    InventoryAuditRecordRepository,
                    InventoryAuditOutboxRepository,
                    InventoryAuditDeadLetterRepository {

        private Long capturedTenantId;
        private Long capturedUserId;

        private final Inventory stock = Inventory.reconstruct(
                InventoryId.of(1L),
                SkuId.of(101L),
                WarehouseCode.of("DEFAULT"),
                new OnHandQuantity(100),
                new ReservedQuantity(10),
                InventoryStatus.ENABLED,
                new Version(1L),
                Instant.parse("2026-03-26T10:00:00Z"));

        @Override
        public Optional<Inventory> findBySkuId(SkuId skuId) {
            captureContext();
            return Optional.of(stock);
        }

        @Override
        public List<Inventory> list() {
            captureContext();
            return List.of(stock);
        }

        @Override
        public List<Inventory> listBySkuIds(Set<SkuId> skuIds) {
            captureContext();
            return List.of(stock);
        }

        @Override
        public List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            captureContext();
            return List.of(stock);
        }

        @Override
        public long count(SkuId skuId, InventoryStatus status) {
            captureContext();
            return 1;
        }

        @Override
        public Inventory insert(Inventory inventory) {
            return inventory;
        }

        @Override
        public Inventory update(Inventory inventory) {
            return inventory;
        }

        @Override
        public InventoryReservation insert(InventoryReservation reservation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InventoryReservation update(InventoryReservation reservation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<InventoryReservation> findByOrderNo(OrderNo orderNo) {
            captureContext();
            return Optional.empty();
        }

        @Override
        public void insertLedger(InventoryLedger ledger) {}

        @Override
        public List<InventoryLedger> listLedgers(OrderNo orderNo) {
            captureContext();
            return List.of();
        }

        @Override
        public void insertLog(InventoryAuditLog auditLog) {}

        @Override
        public List<InventoryAuditLog> listLogs(OrderNo orderNo) {
            captureContext();
            return List.of();
        }

        @Override
        public void insert(InventoryAuditOutbox outbox) {}

        @Override
        public void insert(InventoryAuditDeadLetter deadLetter) {}

        private void captureContext() {
            capturedTenantId = BaconContextHolder.currentTenantId();
            capturedUserId = BaconContextHolder.currentUserId();
        }
    }
}
