package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryBatchQueryRequest;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryTenantScopedRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventories")
@Tag(name = "Inventory-Management", description = "库存查询接口")
public class InventoryController {

    private final InventoryQueryService inventoryQueryService;

    public InventoryController(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping("/{skuId}")
    public InventoryStockResponse getInventory(@PathVariable @Positive Long skuId,
                                               @Valid @ModelAttribute InventoryTenantScopedRequest request) {
        return InventoryStockResponse.from(
                inventoryQueryService.getAvailableStock(request.getTenantId(), skuId)
        );
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @HasPermission("inventory:stock:view")
    @GetMapping
    public List<InventoryStockResponse> listInventories(@Valid @ModelAttribute InventoryBatchQueryRequest request) {
        return inventoryQueryService.batchGetAvailableStock(request.getTenantId(), request.getSkuIds()).stream()
                .map(InventoryStockResponse::from)
                .toList();
    }
}
