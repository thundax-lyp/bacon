package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/inventory")
public class InventoryProviderController {

    private final InventoryReadFacade inventoryReadFacade;
    private final InventoryCommandFacade inventoryCommandFacade;

    public InventoryProviderController(InventoryReadFacade inventoryReadFacade, InventoryCommandFacade inventoryCommandFacade) {
        this.inventoryReadFacade = inventoryReadFacade;
        this.inventoryCommandFacade = inventoryCommandFacade;
    }

    @GetMapping("/stocks/{skuId}")
    public InventoryStockDTO getAvailableStock(@RequestParam("tenantId") Long tenantId, @PathVariable Long skuId) {
        return inventoryReadFacade.getAvailableStock(tenantId, skuId);
    }

    @GetMapping("/stocks")
    public List<InventoryStockDTO> batchGetAvailableStock(@RequestParam("tenantId") Long tenantId,
                                                          @RequestParam("skuIds") Set<Long> skuIds) {
        return inventoryReadFacade.batchGetAvailableStock(tenantId, skuIds);
    }

    @GetMapping("/reservations/{orderNo}")
    public InventoryReservationDTO getReservation(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryReadFacade.getReservationByOrderNo(tenantId, orderNo);
    }

    @PostMapping("/reservations/{orderNo}/reserve")
    public InventoryReservationResultDTO reserve(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestBody List<InventoryReservationItemDTO> items) {
        return inventoryCommandFacade.reserveStock(tenantId, orderNo, items);
    }

    @PostMapping("/reservations/{orderNo}/release")
    public InventoryReservationResultDTO release(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo,
                                                 @RequestParam("reason") String reason) {
        return inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, reason);
    }

    @PostMapping("/reservations/{orderNo}/deduct")
    public InventoryReservationResultDTO deduct(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return inventoryCommandFacade.deductReservedStock(tenantId, orderNo);
    }
}
