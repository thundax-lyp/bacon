package com.github.thundax.bacon.inventory.interfaces.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTaskApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class InventoryAuditCompensationControllerContractTest {

    private static final IdGenerator ID_GENERATOR = bizTag -> 1L;

    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        StubReplayTaskRepository replayTaskRepository = new StubReplayTaskRepository();
        InventoryQueryApplicationService inventoryQueryService =
                new InventoryQueryApplicationService(null, null, null, new StubAuditDeadLetterRepository());
        InventoryAuditReplayTaskApplicationService replayTaskService =
                new InventoryAuditReplayTaskApplicationService(replayTaskRepository, ID_GENERATOR);
        InventoryAuditCompensationController controller =
                new InventoryAuditCompensationController(inventoryQueryService, null, replayTaskService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
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

    @Test
    void shouldCreateReplayTaskWithoutTenantIdInResponse() throws Exception {
        mockMvc.perform(
                        post("/inventory-audit-dead-letters/replay-tasks")
                                .contentType(APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "operatorId": 3001,
                                  "replayKeyPrefix": "TASK",
                                  "deadLetterIds": [1001]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isNumber())
                .andExpect(jsonPath("$.taskNo").isString())
                .andExpect(jsonPath("$.tenantId").doesNotExist());
    }

    private static final class StubAuditDeadLetterRepository implements InventoryAuditDeadLetterRepository {

        private final InventoryAuditDeadLetter deadLetter = InventoryAuditDeadLetter.reconstruct(
                DeadLetterId.of(1001L),
                OutboxId.of(2001L),
                EventCode.of("INV-AUDIT-001"),
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
        public List<InventoryAuditDeadLetter> pageAuditDeadLetters(
                OrderNo orderNo, InventoryAuditReplayStatus replayStatus, int pageNo, int pageSize) {
            return List.of(deadLetter);
        }

        @Override
        public long countAuditDeadLetters(OrderNo orderNo, InventoryAuditReplayStatus replayStatus) {
            return 1L;
        }
    }

    private static final class StubReplayTaskRepository implements InventoryAuditReplayTaskRepository {

        private final Map<Long, InventoryAuditReplayTask> tasks = new ConcurrentHashMap<>();
        private final Map<Long, Long> taskTenants = new ConcurrentHashMap<>();
        private final Map<Long, List<InventoryAuditReplayTaskItem>> taskItems = new ConcurrentHashMap<>();

        @Override
        public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
            Long taskId = task.getId() == null ? null : task.getId().value();
            tasks.put(taskId, task);
            taskTenants.put(taskId, BaconContextHolder.requireTenantId());
            return task;
        }

        @Override
        public void batchSaveAuditReplayTaskItems(List<InventoryAuditReplayTaskItem> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            taskItems.computeIfAbsent(
                            items.get(0).getTaskId() == null ? null : items.get(0).getTaskId().value(),
                            key -> new ArrayList<>())
                    .addAll(items);
        }

        @Override
        public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
            return Optional.ofNullable(tasks.get(taskId == null ? null : taskId.value()));
        }

        @Override
        public Long findAuditReplayTaskTenantId(TaskId taskId) {
            return taskTenants.get(taskId == null ? null : taskId.value());
        }

        @Override
        public boolean pauseAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant pausedAt) {
            Long tenantId = BaconContextHolder.requireTenantId();
            return findAuditReplayTaskById(taskId)
                    .filter(task -> java.util.Objects.equals(
                            tenantId, taskTenants.get(task.getId() == null ? null : task.getId().value())))
                    .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                            || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                    .map(task -> {
                        task.pause(pausedAt);
                        return true;
                    })
                    .orElse(false);
        }

        @Override
        public boolean resumeAuditReplayTask(TaskId taskId, OperatorId operatorId, Instant updatedAt) {
            Long tenantId = BaconContextHolder.requireTenantId();
            return findAuditReplayTaskById(taskId)
                    .filter(task -> java.util.Objects.equals(
                            tenantId, taskTenants.get(task.getId() == null ? null : task.getId().value())))
                    .filter(task -> InventoryAuditReplayTaskStatus.PAUSED.equals(task.getStatus()))
                    .map(task -> {
                        task.resume(updatedAt);
                        return true;
                    })
                    .orElse(false);
        }
    }
}
