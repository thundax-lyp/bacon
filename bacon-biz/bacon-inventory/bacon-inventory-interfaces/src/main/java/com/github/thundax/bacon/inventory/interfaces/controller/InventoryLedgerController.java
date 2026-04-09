package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryOrderScopedRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryLedgerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventory-ledgers")
@Tag(name = "Inventory-Ledger", description = "库存流水查询接口")
public class InventoryLedgerController {

    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryLedgerController(InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "按订单号查询库存流水")
    @HasPermission("inventory:ledger:view")
    @GetMapping
    public List<InventoryLedgerResponse> listByOrderNo(
            @CurrentTenant Long tenantId, @Valid @ModelAttribute InventoryOrderScopedRequest request) {
        return inventoryQueryService
                .listLedgersByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(request.getOrderNo()))
                .stream()
                .map(InventoryLedgerResponse::from)
                .toList();
    }
}
