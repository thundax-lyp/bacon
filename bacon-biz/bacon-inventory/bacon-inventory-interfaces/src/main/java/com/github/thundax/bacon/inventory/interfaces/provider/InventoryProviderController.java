package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.service.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/providers/inventory")
@Tag(name = "Inner-Inventory-Management", description = "Inventory 域内部 Provider 接口")
public class InventoryProviderController {

    private final InventoryQueryService inventoryQueryService;
    private final InventoryApplicationService inventoryApplicationService;

    public InventoryProviderController(InventoryQueryService inventoryQueryService,
                                       InventoryApplicationService inventoryApplicationService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @Operation(summary = "查询 SKU 可用库存")
    @GetMapping("/stocks/{skuId}")
    public InventoryStockDTO getAvailableStock(@RequestParam("tenantId") Long tenantId, @PathVariable Long skuId) {
        return inventoryQueryService.getAvailableStock(tenantId, skuId);
    }

    @Operation(summary = "批量查询 SKU 可用库存")
    @GetMapping("/stocks")
    public List<InventoryStockDTO> batchGetAvailableStock(@RequestParam("tenantId") Long tenantId,
                                                          @RequestParam("skuIds") Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(tenantId, skuIds);
    }

    @Operation(summary = "按订单号查询库存预占结果")
    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationDTO getReservation(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(tenantId, orderNo);
    }

    @Operation(summary = "预占库存")
    @PostMapping("/reservations/{orderNo}/reserve")
    public InventoryReservationResultDTO reserve(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestBody List<InventoryReservationItemDTO> items) {
        return inventoryApplicationService.reserveStock(tenantId, orderNo, items);
    }

    @Operation(summary = "释放预占库存")
    @PostMapping("/reservations/{orderNo}/release")
    public InventoryReservationResultDTO release(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestParam("reason") String reason) {
        return inventoryApplicationService.releaseReservedStock(tenantId, orderNo, reason);
    }

    @Operation(summary = "扣减预占库存")
    @PostMapping("/reservations/{orderNo}/deduct")
    public InventoryReservationResultDTO deduct(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryApplicationService.deductReservedStock(tenantId, orderNo);
    }
}
