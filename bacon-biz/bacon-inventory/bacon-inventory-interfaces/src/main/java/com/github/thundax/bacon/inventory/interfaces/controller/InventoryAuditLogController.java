package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.CurrentTenant;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryOrderScopedRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryAuditLogResponse;
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
@RequestMapping("/inventory-audit-logs")
@Tag(name = "Inventory-Audit", description = "库存审计日志查询接口")
public class InventoryAuditLogController {

    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryAuditLogController(InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "按订单号查询库存审计日志")
    @HasPermission("inventory:audit:view")
    @GetMapping
    public List<InventoryAuditLogResponse> listByOrderNo(
            @CurrentTenant Long tenantId, @Valid @ModelAttribute InventoryOrderScopedRequest request) {
        return inventoryQueryService
                .listAuditLogsByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(request.getOrderNo()))
                .stream()
                .map(InventoryAuditLogResponse::from)
                .toList();
    }
}
