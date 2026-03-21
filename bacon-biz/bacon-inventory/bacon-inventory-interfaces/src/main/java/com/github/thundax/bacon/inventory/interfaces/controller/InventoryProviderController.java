package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.service.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/providers/inventory")
public class InventoryProviderController {

    private final InventoryQueryService inventoryQueryService;
    private final InventoryApplicationService inventoryApplicationService;

    public InventoryProviderController(InventoryQueryService inventoryQueryService,
                                       InventoryApplicationService inventoryApplicationService) {
        this.inventoryQueryService = inventoryQueryService;
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @GetMapping("/stocks/{skuId}")
    public InventoryStockDTO getAvailableStock(@RequestParam("tenantId") Long tenantId, @PathVariable Long skuId) {
        return inventoryQueryService.getAvailableStock(tenantId, skuId);
    }

    @GetMapping("/stocks")
    public List<InventoryStockDTO> batchGetAvailableStock(@RequestParam("tenantId") Long tenantId,
                                                          @RequestParam("skuIds") Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(tenantId, skuIds);
    }

    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationDTO getReservation(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(tenantId, orderNo);
    }

    @PostMapping("/reservations/{orderNo}/reserve")
    public InventoryReservationResultDTO reserve(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestBody List<InventoryReservationItemDTO> items) {
        return inventoryApplicationService.reserveStock(tenantId, orderNo, items);
    }

    @PostMapping("/reservations/{orderNo}/release")
    public InventoryReservationResultDTO release(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestParam("reason") String reason) {
        return inventoryApplicationService.releaseReservedStock(tenantId, orderNo, reason);
    }

    @PostMapping("/reservations/{orderNo}/deduct")
    public InventoryReservationResultDTO deduct(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryApplicationService.deductReservedStock(tenantId, orderNo);
    }
}
