package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryAuditReplayTaskWorker {

    private final InventoryAuditReplayTaskRepository inventoryAuditReplayTaskRepository;
    private final InventoryAuditReplayTaskApplicationService inventoryAuditReplayTaskService;
    private final InventoryAuditCompensationApplicationService inventoryAuditCompensationService;

    @Value("${bacon.inventory.audit.replay-task.enabled:true}")
    private boolean enabled;

    @Value("${bacon.inventory.audit.replay-task.claim-size:2}")
    private int claimSize;

    @Value("${bacon.inventory.audit.replay-task.batch-size:20}")
    private int batchSize;

    @Value("${bacon.inventory.audit.replay-task.lease-seconds:60}")
    private long leaseSeconds;

    @Value("${spring.application.name:bacon-inventory}")
    private String applicationName;

    private final String ownerSuffix = UUID.randomUUID().toString();

    public InventoryAuditReplayTaskWorker(
            InventoryAuditReplayTaskRepository inventoryAuditReplayTaskRepository,
            InventoryAuditReplayTaskApplicationService inventoryAuditReplayTaskService,
            InventoryAuditCompensationApplicationService inventoryAuditCompensationService) {
        this.inventoryAuditReplayTaskRepository = inventoryAuditReplayTaskRepository;
        this.inventoryAuditReplayTaskService = inventoryAuditReplayTaskService;
        this.inventoryAuditCompensationService = inventoryAuditCompensationService;
    }

    @Scheduled(fixedDelayString = "${bacon.inventory.audit.replay-task.fixed-delay-ms:3000}")
    public void consumeReplayTasks() {
        if (!enabled) {
            return;
        }
        Instant now = Instant.now();
        String owner = applicationName + ":" + ownerSuffix;
        List<InventoryAuditReplayTask> tasks = inventoryAuditReplayTaskRepository.claimRunnableAuditReplayTasks(
                now, Math.max(claimSize, 1), owner, now.plusSeconds(Math.max(leaseSeconds, 1L)));
        if (tasks.isEmpty()) {
            return;
        }
        for (InventoryAuditReplayTask task : tasks) {
            try {
                inventoryAuditReplayTaskService.processClaimedTask(
                        task, inventoryAuditCompensationService, owner, batchSize, leaseSeconds);
            } catch (RuntimeException ex) {
                Metrics.counter("bacon.inventory.audit.replay.task.worker.fail.total")
                        .increment();
                log.error(
                        "ALERT inventory audit replay task worker failed, taskId={}, taskNo={}",
                        task.getIdValue(),
                        task.getTaskNoValue(),
                        ex);
            }
        }
    }
}
