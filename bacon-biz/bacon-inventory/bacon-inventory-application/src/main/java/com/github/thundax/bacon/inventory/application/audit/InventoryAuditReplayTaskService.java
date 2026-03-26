package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryAuditReplayTaskService {

    private static final String OPERATOR_TYPE_MANUAL = "MANUAL";

    private final InventoryLogRepository inventoryLogRepository;

    public InventoryAuditReplayTaskService(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public InventoryAuditReplayTaskDTO createReplayTask(InventoryAuditReplayTaskCreateDTO createDTO) {
        if (createDTO.getDeadLetterIds() == null || createDTO.getDeadLetterIds().isEmpty()) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_BAD_REQUEST,
                    "replay-task-empty-dead-letter-ids");
        }
        Instant now = Instant.now();
        InventoryAuditReplayTask task = new InventoryAuditReplayTask(null, createDTO.getTenantId(),
                "RPT-" + UUID.randomUUID().toString().replace("-", ""), InventoryAuditReplayTask.STATUS_PENDING,
                createDTO.getDeadLetterIds().size(), 0, 0, 0, createDTO.getReplayKeyPrefix(), OPERATOR_TYPE_MANUAL,
                createDTO.getOperatorId(), null, null, null, now, null, null, null, now);
        InventoryAuditReplayTask saved = inventoryLogRepository.saveAuditReplayTask(task);
        inventoryLogRepository.batchSaveAuditReplayTaskItems(saved.getId(), saved.getTenantId(),
                createDTO.getDeadLetterIds(), now);
        Metrics.counter("bacon.inventory.audit.replay.task.created.total").increment();
        return toDto(saved);
    }

    public InventoryAuditReplayTaskDTO getReplayTask(Long tenantId, Long taskId) {
        InventoryAuditReplayTask task = getTaskById(taskId);
        ensureTaskTenant(task, tenantId);
        return toDto(task);
    }

    public InventoryAuditReplayTaskDTO pauseReplayTask(Long tenantId, Long taskId, Long operatorId) {
        InventoryAuditReplayTask task = getTaskById(taskId);
        ensureTaskTenant(task, tenantId);
        if (isTerminal(task.getStatus())) {
            return toDto(task);
        }
        Instant now = Instant.now();
        boolean paused = inventoryLogRepository.pauseAuditReplayTask(taskId, tenantId, operatorId, now);
        if (paused) {
            Metrics.counter("bacon.inventory.audit.replay.task.paused.total").increment();
        }
        return toDto(getTaskById(taskId));
    }

    public InventoryAuditReplayTaskDTO resumeReplayTask(Long tenantId, Long taskId, Long operatorId) {
        InventoryAuditReplayTask task = getTaskById(taskId);
        ensureTaskTenant(task, tenantId);
        if (isTerminal(task.getStatus())) {
            return toDto(task);
        }
        Instant now = Instant.now();
        boolean resumed = inventoryLogRepository.resumeAuditReplayTask(taskId, tenantId, operatorId, now);
        if (resumed) {
            Metrics.counter("bacon.inventory.audit.replay.task.resumed.total").increment();
        }
        return toDto(getTaskById(taskId));
    }

    public void processClaimedTask(InventoryAuditReplayTask task, InventoryAuditCompensationService compensationService,
                                   String processingOwner, int batchSize, long leaseSeconds) {
        if (task == null || !InventoryAuditReplayTask.STATUS_RUNNING.equals(task.getStatus())) {
            return;
        }
        if (!Objects.equals(task.getProcessingOwner(), processingOwner)) {
            return;
        }
        Instant now = Instant.now();
        inventoryLogRepository.renewAuditReplayTaskLease(task.getId(), processingOwner,
                now.plusSeconds(Math.max(leaseSeconds, 1L)), now);

        List<InventoryAuditReplayTaskItem> items = inventoryLogRepository.findPendingAuditReplayTaskItems(task.getId(),
                Math.max(batchSize, 1));
        if (items.isEmpty()) {
            finishTask(task.getId(), processingOwner);
            return;
        }

        int processedDelta = 0;
        int successDelta = 0;
        int failedDelta = 0;
        String lastError = null;
        for (InventoryAuditReplayTaskItem item : items) {
            Instant startedAt = Instant.now();
            try {
                String replayKey = buildReplayKey(task, item);
                InventoryAuditReplayResultDTO result = compensationService.replayDeadLetter(task.getTenantId(),
                        item.getDeadLetterId(), replayKey, task.getOperatorId());
                String itemStatus = InventoryAuditReplayTaskItem.STATUS_FAILED;
                if (InventoryAuditReplayTaskItem.STATUS_SUCCEEDED.equals(result.getReplayStatus())) {
                    itemStatus = InventoryAuditReplayTaskItem.STATUS_SUCCEEDED;
                    successDelta++;
                } else {
                    failedDelta++;
                    lastError = result.getMessage();
                }
                inventoryLogRepository.markAuditReplayTaskItemResult(item.getId(), itemStatus, result.getReplayStatus(),
                        result.getReplayKey(), result.getMessage(), startedAt, Instant.now());
            } catch (RuntimeException ex) {
                failedDelta++;
                lastError = truncateError(ex.getMessage());
                inventoryLogRepository.markAuditReplayTaskItemResult(item.getId(), InventoryAuditReplayTaskItem.STATUS_FAILED,
                        InventoryAuditReplayTaskItem.STATUS_FAILED, null, "failed:" + lastError, startedAt, Instant.now());
            }
            processedDelta++;
        }
        inventoryLogRepository.incrementAuditReplayTaskProgress(task.getId(), processingOwner, processedDelta,
                successDelta, failedDelta, Instant.now());
        if (lastError != null) {
            Metrics.counter("bacon.inventory.audit.replay.task.item.failed.total").increment(failedDelta);
        }

        List<InventoryAuditReplayTaskItem> remain = inventoryLogRepository.findPendingAuditReplayTaskItems(task.getId(), 1);
        if (remain.isEmpty()) {
            finishTask(task.getId(), processingOwner);
        }
    }

    private void finishTask(Long taskId, String processingOwner) {
        InventoryAuditReplayTask latest = getTaskById(taskId);
        String status = Integer.valueOf(0).equals(latest.getFailedCount())
                ? InventoryAuditReplayTask.STATUS_SUCCEEDED
                : InventoryAuditReplayTask.STATUS_FAILED;
        inventoryLogRepository.finishAuditReplayTask(taskId, processingOwner, status, latest.getLastError(), Instant.now());
        Metrics.counter("bacon.inventory.audit.replay.task.finished.total", "status", status).increment();
    }

    private String buildReplayKey(InventoryAuditReplayTask task, InventoryAuditReplayTaskItem item) {
        if (task.getReplayKeyPrefix() == null || task.getReplayKeyPrefix().isBlank()) {
            return "TASK-" + task.getTaskNo() + "-DL-" + item.getDeadLetterId();
        }
        return task.getReplayKeyPrefix() + "-" + item.getDeadLetterId();
    }

    private String truncateError(String error) {
        if (error == null || error.isBlank()) {
            return "UNKNOWN";
        }
        return error.length() <= 512 ? error : error.substring(0, 512);
    }

    private InventoryAuditReplayTask getTaskById(Long taskId) {
        return inventoryLogRepository.findAuditReplayTaskById(taskId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND,
                        "replay-task-not-found:" + taskId));
    }

    private void ensureTaskTenant(InventoryAuditReplayTask task, Long tenantId) {
        if (!Objects.equals(task.getTenantId(), tenantId)) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN,
                    "replay-task-tenant-mismatch");
        }
    }

    private boolean isTerminal(String status) {
        return InventoryAuditReplayTask.STATUS_SUCCEEDED.equals(status)
                || InventoryAuditReplayTask.STATUS_FAILED.equals(status)
                || InventoryAuditReplayTask.STATUS_CANCELED.equals(status);
    }

    private InventoryAuditReplayTaskDTO toDto(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDTO(task.getId(), task.getTenantId(), task.getTaskNo(), task.getStatus(),
                task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(), task.getFailedCount(),
                task.getReplayKeyPrefix(), task.getOperatorId(), task.getLastError(), task.getCreatedAt(),
                task.getStartedAt(), task.getPausedAt(), task.getFinishedAt(), task.getUpdatedAt());
    }
}
