package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.web.resolver.CurrentTenantArgumentResolver;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryAuditCompensationControllerContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InventoryQueryApplicationService inventoryQueryService = new InventoryQueryApplicationService(
                null, null, null, new StubAuditDeadLetterRepository());
        InventoryAuditCompensationController controller = new InventoryAuditCompensationController(
                inventoryQueryService, null, null);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new CurrentTenantArgumentResolver(() -> 1001L))
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnPagedDeadLettersWhenRequestIsValid() throws Exception {
        mockMvc.perform(get("/inventory-audit-dead-letters")
                        .param("replayStatus", "FAILED")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.pageNo").value(1))
                .andExpect(jsonPath("$.records[0].id").value(1001))
                .andExpect(jsonPath("$.records[0].replayStatus").value("FAILED"));
    }

    @Test
    void shouldRejectPageRequestWithIllegalReplayStatus() throws Exception {
        mockMvc.perform(get("/inventory-audit-dead-letters")
                        .param("replayStatus", "INVALID")
                        .param("pageNo", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest());
    }

    private static final class StubAuditDeadLetterRepository implements InventoryAuditDeadLetterRepository {

        private final InventoryAuditDeadLetter deadLetter = new InventoryAuditDeadLetter(
                DeadLetterId.of(1001L),
                OutboxId.of(2001L),
                EventCode.of("INV-AUDIT-001"),
                TenantId.of(1001L),
                OrderNo.of("ORDER-001"),
                ReservationNo.of("RSV-001"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.MANUAL,
                "3001",
                Instant.parse("2026-03-26T10:00:00Z"),
                1,
                "simulated-error",
                "dead-letter",
                Instant.parse("2026-03-26T10:01:00Z"),
                InventoryAuditReplayStatus.FAILED,
                2,
                Instant.parse("2026-03-26T10:02:00Z"),
                "FAILED",
                "simulated-replay-error",
                "REPLAY-001",
                "USER",
                "3001");

        @Override
        public List<InventoryAuditDeadLetter> pageAuditDeadLetters(TenantId tenantId, OrderNo orderNo,
                                                                   InventoryAuditReplayStatus replayStatus,
                                                                   int pageNo, int pageSize) {
            return List.of(deadLetter);
        }

        @Override
        public long countAuditDeadLetters(TenantId tenantId, OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
            return 1L;
        }
    }
}
