package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
import com.github.thundax.bacon.inventory.application.mapper.DeadLetterIdMapper;
import com.github.thundax.bacon.inventory.application.mapper.TaskNoMapper;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InventoryAuditReplayTaskApplicationService {

    private static final String OPERATOR_TYPE_MANUAL = "MANUAL";

    private final InventoryAuditReplayTaskRepository inventoryAuditReplayTaskRepository;

    public InventoryAuditReplayTaskApplicationService(InventoryAuditReplayTaskRepository inventoryAuditReplayTaskRepository) {
        this.inventoryAuditReplayTaskRepository = inventoryAuditReplayTaskRepository;
    }

    public InventoryAuditReplayTaskDTO createReplayTask(InventoryAuditReplayTaskCreateDTO createDTO) {
        if (createDTO.getDeadLetterIds() == null || createDTO.getDeadLetterIds().isEmpty()) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_BAD_REQUEST,
                    "replay-task-empty-dead-letter-ids");
        }
        Instant now = Instant.now();
        TaskNo taskNo = TaskNoMapper.toDomain("RPT-" + UUID.randomUUID().toString().replace("-", ""));
        // 回放任务只负责“组织一批死信逐条回放”，真正的单条回放语义仍复用 compensation service。
        InventoryAuditReplayTask task = new InventoryAuditReplayTask(null,
                TenantIdMapper.toDomain(createDTO.getTenantId()),
                taskNo, InventoryAuditReplayTaskStatus.PENDING,
                createDTO.getDeadLetterIds().size(), 0, 0, 0, createDTO.getReplayKeyPrefix(), OPERATOR_TYPE_MANUAL,
                createDTO.getOperatorId() == null ? null : String.valueOf(createDTO.getOperatorId()),
                null, null, null, now, null, null, null, now);
        InventoryAuditReplayTask saved = inventoryAuditReplayTaskRepository.saveAuditReplayTask(task);
        inventoryAuditReplayTaskRepository.batchSaveAuditReplayTaskItems(saved.getIdValue(), saved.getTenantIdValue(),
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
        boolean paused = inventoryAuditReplayTaskRepository.pauseAuditReplayTask(taskId, tenantId, operatorId, now);
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
        boolean resumed = inventoryAuditReplayTaskRepository.resumeAuditReplayTask(taskId, tenantId, operatorId, now);
        if (resumed) {
            Metrics.counter("bacon.inventory.audit.replay.task.resumed.total").increment();
        }
        return toDto(getTaskById(taskId));
    }

    public void processClaimedTask(InventoryAuditReplayTask task, InventoryAuditCompensationApplicationService compensationService,
                                   String processingOwner, int batchSize, long leaseSeconds) {
        if (task == null || !InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus())) {
            return;
        }
        if (!Objects.equals(task.getProcessingOwner(), processingOwner)) {
            return;
        }
        Instant now = Instant.now();
        // Worker 每轮都会续租任务，确保长批次处理时不会因为租约过期被其他节点重复接管。
        inventoryAuditReplayTaskRepository.renewAuditReplayTaskLease(task.getIdValue(), processingOwner,
                now.plusSeconds(Math.max(leaseSeconds, 1L)), now);

        List<InventoryAuditReplayTaskItem> items = inventoryAuditReplayTaskRepository.findPendingAuditReplayTaskItems(task.getIdValue(),
                Math.max(batchSize, 1));
        if (items.isEmpty()) {
            finishTask(task.getIdValue(), processingOwner);
            return;
        }

        // 任务项逐条独立调用单条回放，任何一项失败都只影响自己的结果统计，不中断整批任务推进。
        int processedDelta = 0;
        int successDelta = 0;
        int failedDelta = 0;
        String lastError = null;
        for (InventoryAuditReplayTaskItem item : items) {
            Instant startedAt = Instant.now();
            try {
                String replayKey = buildReplayKey(task, item);
                InventoryAuditReplayResultDTO result = compensationService.replayDeadLetter(task.getTenantId(),
                        DeadLetterIdMapper.toDomain(item.getDeadLetterIdValue()), replayKey, task.getOperatorIdValue());
                InventoryAuditReplayStatus replayStatus = InventoryAuditReplayStatus.from(result.getReplayStatus());
                InventoryAuditReplayTaskItemStatus itemStatus = InventoryAuditReplayTaskItemStatus.FAILED;
                if (InventoryAuditReplayStatus.SUCCEEDED.equals(replayStatus)) {
                    itemStatus = InventoryAuditReplayTaskItemStatus.SUCCEEDED;
                    successDelta++;
                } else {
                    failedDelta++;
                    lastError = result.getMessage();
                }
                inventoryAuditReplayTaskRepository.markAuditReplayTaskItemResult(item.getId(), itemStatus, replayStatus,
                        result.getReplayKey(), result.getMessage(), startedAt, Instant.now());
            } catch (RuntimeException ex) {
                failedDelta++;
                lastError = truncateError(ex.getMessage());
                inventoryAuditReplayTaskRepository.markAuditReplayTaskItemResult(item.getId(),
                        InventoryAuditReplayTaskItemStatus.FAILED, InventoryAuditReplayStatus.FAILED, null,
                        "failed:" + lastError, startedAt, Instant.now());
            }
            processedDelta++;
        }
        inventoryAuditReplayTaskRepository.incrementAuditReplayTaskProgress(task.getIdValue(), processingOwner, processedDelta,
                successDelta, failedDelta, Instant.now());
        if (lastError != null) {
            Metrics.counter("bacon.inventory.audit.replay.task.item.failed.total").increment(failedDelta);
        }

        List<InventoryAuditReplayTaskItem> remain = inventoryAuditReplayTaskRepository.findPendingAuditReplayTaskItems(task.getIdValue(), 1);
        if (remain.isEmpty()) {
            finishTask(task.getIdValue(), processingOwner);
        }
    }

    private void finishTask(Long taskId, String processingOwner) {
        InventoryAuditReplayTask latest = getTaskById(taskId);
        // 整个任务只要存在失败项就标记 FAILED；只有全部成功才算 SUCCEEDED，便于上层按任务粒度判断是否还需人工介入。
        InventoryAuditReplayTaskStatus status = Integer.valueOf(0).equals(latest.getFailedCount())
                ? InventoryAuditReplayTaskStatus.SUCCEEDED
                : InventoryAuditReplayTaskStatus.FAILED;
        inventoryAuditReplayTaskRepository.finishAuditReplayTask(taskId, processingOwner, status.value(), latest.getLastError(), Instant.now());
        Metrics.counter("bacon.inventory.audit.replay.task.finished.total", "status", status.value()).increment();
    }

    private String buildReplayKey(InventoryAuditReplayTask task, InventoryAuditReplayTaskItem item) {
        // 优先复用外部提供的回放前缀；否则退化为任务号 + 死信号，确保同一任务内 replayKey 稳定可追踪。
        if (task.getReplayKeyPrefix() == null || task.getReplayKeyPrefix().isBlank()) {
            return "TASK-" + task.getTaskNoValue() + "-DL-" + item.getDeadLetterIdValue();
        }
        return task.getReplayKeyPrefix() + "-" + item.getDeadLetterIdValue();
    }

    private String truncateError(String error) {
        if (error == null || error.isBlank()) {
            return "UNKNOWN";
        }
        return error.length() <= 512 ? error : error.substring(0, 512);
    }

    private InventoryAuditReplayTask getTaskById(Long taskId) {
        return inventoryAuditReplayTaskRepository.findAuditReplayTaskById(taskId)
                .orElseThrow(() -> new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_NOT_FOUND,
                        "replay-task-not-found:" + taskId));
    }

    private void ensureTaskTenant(InventoryAuditReplayTask task, Long tenantId) {
        if (!Objects.equals(task.getTenantIdValue(), tenantId)) {
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_REMOTE_FORBIDDEN,
                    "replay-task-tenant-mismatch");
        }
    }

    private boolean isTerminal(InventoryAuditReplayTaskStatus status) {
        return InventoryAuditReplayTaskStatus.SUCCEEDED.equals(status)
                || InventoryAuditReplayTaskStatus.FAILED.equals(status)
                || InventoryAuditReplayTaskStatus.CANCELED.equals(status);
    }

    private InventoryAuditReplayTaskDTO toDto(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDTO(task.getIdValue(), task.getTenantIdValue(), task.getTaskNoValue(), task.getStatus().value(),
                task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(), task.getFailedCount(),
                task.getReplayKeyPrefix(), task.getOperatorId(), task.getLastError(), task.getCreatedAt(),
                task.getStartedAt(), task.getPausedAt(), task.getFinishedAt(), task.getUpdatedAt());
    }
}
