package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterPageQueryDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTaskService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventory-audit-dead-letters")
@Tag(name = "Inventory-Audit-Compensation", description = "库存审计死信补偿接口")
public class InventoryAuditCompensationController {

    private final InventoryQueryService inventoryQueryService;
    private final InventoryAuditCompensationService inventoryAuditCompensationService;
    private final InventoryAuditReplayTaskService inventoryAuditReplayTaskService;

    public InventoryAuditCompensationController(InventoryQueryService inventoryQueryService,
                                                InventoryAuditCompensationService inventoryAuditCompensationService,
                                                InventoryAuditReplayTaskService inventoryAuditReplayTaskService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryAuditCompensationService = inventoryAuditCompensationService;
        this.inventoryAuditReplayTaskService = inventoryAuditReplayTaskService;
    }

    @Operation(summary = "分页查询库存审计死信")
    @HasPermission("inventory:audit:dead:view")
    @GetMapping
    public InventoryAuditDeadLetterPageResponse pageDeadLetters(@Valid @ModelAttribute InventoryAuditDeadLetterPageRequest request) {
        return InventoryAuditDeadLetterPageResponse.from(inventoryQueryService.pageAuditDeadLetters(
                new InventoryAuditDeadLetterPageQueryDTO(request.getTenantId(), request.getOrderNo(),
                        request.getReplayStatus(), request.getPageNo(), request.getPageSize())));
    }

    @Operation(summary = "重放单条库存审计死信")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/{deadLetterId}/replay")
    public InventoryAuditReplayResultResponse replayOne(@PathVariable @NotNull @Positive Long deadLetterId,
                                                        @Valid @RequestBody InventoryAuditReplayRequest request) {
        return InventoryAuditReplayResultResponse.from(inventoryAuditCompensationService.replayDeadLetter(
                request.tenantId(), deadLetterId, request.replayKey(), request.operatorId()));
    }

    @Operation(summary = "批量重放库存审计死信")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-batch")
    public List<InventoryAuditReplayResultResponse> replayBatch(@Valid @RequestBody InventoryAuditBatchReplayRequest request) {
        return inventoryAuditCompensationService.replayDeadLettersBatch(request.tenantId(), request.deadLetterIds(),
                        request.replayKeyPrefix(), request.operatorId())
                .stream()
                .map(InventoryAuditReplayResultResponse::from)
                .toList();
    }

    @Operation(summary = "创建库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks")
    public InventoryAuditReplayTaskResponse createReplayTask(@Valid @RequestBody InventoryAuditReplayTaskCreateRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.createReplayTask(
                new InventoryAuditReplayTaskCreateDTO(request.tenantId(), request.operatorId(),
                        request.replayKeyPrefix(), request.deadLetterIds())));
    }

    @Operation(summary = "查询库存审计死信批量重放任务进度")
    @HasPermission("inventory:audit:dead:view")
    @GetMapping("/replay-tasks/{taskId}")
    public InventoryAuditReplayTaskResponse getReplayTask(@PathVariable @NotNull @Positive Long taskId,
                                                          @RequestParam @NotNull @Positive Long tenantId) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.getReplayTask(tenantId, taskId));
    }

    @Operation(summary = "暂停库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks/{taskId}/pause")
    public InventoryAuditReplayTaskResponse pauseReplayTask(@PathVariable @NotNull @Positive Long taskId,
                                                            @Valid @RequestBody InventoryAuditReplayTaskControlRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.pauseReplayTask(
                request.tenantId(), taskId, request.operatorId()));
    }

    @Operation(summary = "恢复库存审计死信批量重放任务")
    @HasPermission("inventory:audit:dead:replay")
    @PostMapping("/replay-tasks/{taskId}/resume")
    public InventoryAuditReplayTaskResponse resumeReplayTask(@PathVariable @NotNull @Positive Long taskId,
                                                             @Valid @RequestBody InventoryAuditReplayTaskControlRequest request) {
        return InventoryAuditReplayTaskResponse.from(inventoryAuditReplayTaskService.resumeReplayTask(
                request.tenantId(), taskId, request.operatorId()));
    }
}
