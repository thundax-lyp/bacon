package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping
public class InventoryQueryController {

    private final InventoryQueryService inventoryQueryService;

    public InventoryQueryController(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @GetMapping("/inventories/{skuId}")
    public InventoryStockDTO getInventory(@RequestParam("tenantId") Long tenantId, @PathVariable Long skuId) {
        return inventoryQueryService.getAvailableStock(tenantId, skuId);
    }

    @GetMapping("/inventories")
    public List<InventoryStockDTO> listInventories(@RequestParam("tenantId") Long tenantId,
                                                   @RequestParam("skuIds") Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(tenantId, skuIds);
    }

    @GetMapping("/inventory-reservations/{orderNo}")
    public InventoryReservationDTO getReservation(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(tenantId, orderNo);
    }
}
