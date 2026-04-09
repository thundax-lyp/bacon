package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.commerce.mapper.SkuIdMapper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.command.InventoryManagementApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.interfaces.dto.CreateInventoryRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryBatchQueryRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryPageRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryStatusUpdateRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryPageResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventories")
@Tag(name = "Inventory-Management", description = "库存查询接口")
public class InventoryController {

    private final InventoryManagementApplicationService inventoryManagementApplicationService;
    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryController(
            InventoryManagementApplicationService inventoryManagementApplicationService,
            InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryManagementApplicationService = inventoryManagementApplicationService;
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "新增库存主数据")
    @HasPermission("inventory:stock:create")
    @PostMapping
    public InventoryStockResponse createInventory(
            @CurrentTenant @NotNull @Positive Long tenantId, @Valid @RequestBody CreateInventoryRequest request) {
        InventoryStatus status = parseInventoryStatus(request.status());
        return InventoryStockResponse.from(inventoryManagementApplicationService.createInventory(
                TenantId.of(tenantId), SkuIdMapper.toDomain(request.skuId()), request.onHandQuantity(), status));
    }

    @Operation(summary = "查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping("/{skuId}")
    public InventoryStockResponse getInventory(@CurrentTenant Long tenantId, @PathVariable @Positive Long skuId) {
        return InventoryStockResponse.from(
                inventoryQueryService.getAvailableStock(TenantId.of(tenantId), SkuIdMapper.toDomain(skuId)));
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping
    public List<InventoryStockResponse> listInventories(
            @CurrentTenant Long tenantId, @Valid @ModelAttribute InventoryBatchQueryRequest request) {
        return inventoryQueryService
                .batchGetAvailableStock(
                        TenantId.of(tenantId),
                        request.getSkuIds() == null
                                ? java.util.Set.of()
                                : request.getSkuIds().stream()
                                        .map(SkuIdMapper::toDomain)
                                        .collect(Collectors.toSet()))
                .stream()
                .map(InventoryStockResponse::from)
                .toList();
    }

    @Operation(summary = "分页查询库存主数据")
    @HasPermission("inventory:stock:view")
    @GetMapping("/page")
    public InventoryPageResponse pageInventories(
            @CurrentTenant Long tenantId, @Valid @ModelAttribute InventoryPageRequest request) {
        InventoryStatus status = parseInventoryStatus(request.getStatus());
        return InventoryPageResponse.from(inventoryQueryService.pageInventories(
                TenantId.of(tenantId),
                SkuIdMapper.toDomain(request.getSkuId()),
                status,
                request.getPageNo(),
                request.getPageSize()));
    }

    @Operation(summary = "修改库存状态")
    @HasPermission("inventory:stock:update")
    @PutMapping("/{skuId}/status")
    public InventoryStockResponse updateInventoryStatus(
            @CurrentTenant @NotNull @Positive Long tenantId,
            @PathVariable @Positive Long skuId,
            @Valid @RequestBody InventoryStatusUpdateRequest request) {
        InventoryStatus status = parseInventoryStatus(request.status());
        return InventoryStockResponse.from(inventoryManagementApplicationService.updateInventoryStatus(
                TenantId.of(tenantId), SkuIdMapper.toDomain(skuId), status));
    }

    private InventoryStatus parseInventoryStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return InventoryStatus.from(status);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
