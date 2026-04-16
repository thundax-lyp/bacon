package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.id.codec.OperatorIdCodec;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTaskApplicationService;
import com.github.thundax.bacon.inventory.application.codec.DeadLetterIdCodec;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.codec.TaskIdCodec;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryAuditBatchReplayRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryAuditDeadLetterPageRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryAuditReplayRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryAuditReplayTaskControlRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryAuditReplayTaskCreateRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryAuditDeadLetterPageResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryAuditReplayResultResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryAuditReplayTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventory/inventory-audit-dead-letters")
@Tag(name = "Inventory-Audit-Compensation", description = "库存审计死信补偿接口")
public class InventoryAuditCompensationController {

    private final InventoryQueryApplicationService inventoryQueryService;
    private final InventoryAuditCompensationApplicationService inventoryAuditCompensationService;
    private final InventoryAuditReplayTaskApplicationService inventoryAuditReplayTaskService;

    public InventoryAuditCompensationController(
            InventoryQueryApplicationService inventoryQueryService,
            InventoryAuditCompensationApplicationService inventoryAuditCompensationService,
            InventoryAuditReplayTaskApplicationService inventoryAuditReplayTaskService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryAuditCompensationService = inventoryAuditCompensationService;
        this.inventoryAuditReplayTaskService = inventoryAuditReplayTaskService;
    }

    @Operation(summary = "分页查询库存审计死信")
    @HasPermission("inventory:audit:dead:view")
    @GetMapping
    public InventoryAuditDeadLetterPageResponse pageDeadLetters(
            @Valid @ModelAttribute InventoryAuditDeadLetterPageRequest request) {
        InventoryAuditReplayStatus replayStatus =
                request.getReplayStatus() == null ? null : InventoryAuditReplayStatus.from(request.getReplayStatus());
        return InventoryAuditDeadLetterPageResponse.from(inventoryQueryService.pageAuditDeadLetters(
                OrderNoCodec.toDomain(request.getOrderNo()), replayStatus, request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "重放单条库存审计死信")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/{deadLetterId}/replay")
    public InventoryAuditReplayResultResponse replayOne(
            @PathVariable @NotNull @Positive Long deadLetterId,
            @Valid @RequestBody InventoryAuditReplayRequest request) {
        return InventoryAuditReplayResultResponse.from(inventoryAuditCompensationService.replayDeadLetter(
                DeadLetterIdCodec.toDomain(deadLetterId),
                request.replayKey(),
                OperatorIdCodec.toDomain(request.operatorId() == null ? null : String.valueOf(request.operatorId()))));
    }

    @Operation(summary = "批量重放库存审计死信")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-batch")
    public List<InventoryAuditReplayResultResponse> replayBatch(
            @Valid @RequestBody InventoryAuditBatchReplayRequest request) {
        return inventoryAuditCompensationService
                .replayDeadLettersBatch(
                        request.deadLetterIds() == null
                                ? List.of()
                                : request.deadLetterIds().stream()
                                        .map(DeadLetterIdCodec::toDomain)
                                        .toList(),
                        request.replayKeyPrefix(),
                        OperatorIdCodec.toDomainFromLong(request.operatorId()))
                .stream()
                .map(InventoryAuditReplayResultResponse::from)
                .toList();
    }

    @Operation(summary = "创建库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks")
    public InventoryAuditReplayTaskResponse createReplayTask(
            @Valid @RequestBody InventoryAuditReplayTaskCreateRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.createReplayTask(
                request.operatorId(), request.replayKeyPrefix(), request.deadLetterIds()));
    }

    @Operation(summary = "查询库存审计死信批量重放任务进度")
    @HasPermission("inventory:audit:dead:view")
    @GetMapping("/replay-tasks/{taskId}")
    public InventoryAuditReplayTaskResponse getReplayTask(@PathVariable @NotNull @Positive Long taskId) {
        return InventoryAuditReplayTaskResponse.from(
                inventoryAuditReplayTaskService.getReplayTask(TaskIdCodec.toDomain(taskId)));
    }

    @Operation(summary = "暂停库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks/{taskId}/pause")
    public InventoryAuditReplayTaskResponse pauseReplayTask(
            @PathVariable @NotNull @Positive Long taskId,
            @Valid @RequestBody InventoryAuditReplayTaskControlRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.pauseReplayTask(
                TaskIdCodec.toDomain(taskId), OperatorIdCodec.toDomainFromLong(request.operatorId())));
    }

    @Operation(summary = "恢复库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks/{taskId}/resume")
    public InventoryAuditReplayTaskResponse resumeReplayTask(
            @PathVariable @NotNull @Positive Long taskId,
            @Valid @RequestBody InventoryAuditReplayTaskControlRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.resumeReplayTask(
                TaskIdCodec.toDomain(taskId), OperatorIdCodec.toDomainFromLong(request.operatorId())));
    }
}
