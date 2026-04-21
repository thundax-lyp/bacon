package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryInterfaceAssembler;
import com.github.thundax.bacon.inventory.interfaces.request.CreateInventoryRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryBatchQueryRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryPageRequest;
import com.github.thundax.bacon.inventory.interfaces.request.InventoryStatusUpdateRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryPageResponse;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryStockResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventory/stocks")
@Tag(name = "Inventory-Management", description = "库存查询接口")
public class InventoryController {

    private final InventoryCommandApplicationService inventoryCommandApplicationService;
    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryController(
            InventoryCommandApplicationService inventoryCommandApplicationService,
            InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryCommandApplicationService = inventoryCommandApplicationService;
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "新增库存主数据")
    @HasPermission("inventory:stock:create")
    @PostMapping
    public InventoryStockResponse createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        return InventoryStockResponse.from(inventoryCommandApplicationService.create(
                InventoryInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping("/{skuId}")
    public InventoryStockResponse getInventory(@PathVariable @Positive Long skuId) {
        return InventoryStockResponse.from(
                inventoryQueryService.getAvailableStock(InventoryInterfaceAssembler.toAvailableStockQuery(skuId)));
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping
    public List<InventoryStockResponse> listInventories(@Valid @ModelAttribute InventoryBatchQueryRequest request) {
        return inventoryQueryService
                .batchGetAvailableStock(InventoryInterfaceAssembler.toBatchAvailableStockQuery(request))
                .stream()
                .map(InventoryStockResponse::from)
                .toList();
    }

    @Operation(summary = "分页查询库存主数据")
    @HasPermission("inventory:stock:view")
    @GetMapping("/page")
    public InventoryPageResponse page(@Valid @ModelAttribute InventoryPageRequest request) {
        return InventoryPageResponse.from(inventoryQueryService.page(
                InventoryInterfaceAssembler.toPageQuery(request)));
    }

    @Operation(summary = "修改库存状态")
    @HasPermission("inventory:stock:update")
    @PutMapping("/{skuId}/status")
    public InventoryStockResponse updateInventoryStatus(
            @PathVariable @Positive Long skuId, @Valid @RequestBody InventoryStatusUpdateRequest request) {
        return InventoryStockResponse.from(
                inventoryCommandApplicationService.updateStatus(
                        InventoryInterfaceAssembler.toStatusUpdateCommand(skuId, request)));
    }
}
