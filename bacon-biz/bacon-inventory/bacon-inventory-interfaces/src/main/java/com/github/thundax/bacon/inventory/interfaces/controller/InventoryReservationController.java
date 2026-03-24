package com.github.thundax.bacon.inventory.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryAssembler;
import com.github.thundax.bacon.inventory.interfaces.dto.InventoryTenantScopedRequest;
import com.github.thundax.bacon.inventory.interfaces.response.InventoryReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/inventory-reservations")
@Tag(name = "Inventory-Reservation", description = "库存预占查询接口")
public class InventoryReservationController {

    private final InventoryQueryService inventoryQueryService;

    public InventoryReservationController(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Operation(summary = "按订单号查询库存预占结果")
    @HasPermission("inventory:reservation:view")
    @GetMapping("/{orderNo}")
    public InventoryReservationResponse getReservation(@PathVariable @NotBlank String orderNo,
                                                       @Valid @ModelAttribute InventoryTenantScopedRequest request) {
        return InventoryAssembler.toReservationResponse(
                inventoryQueryService.getReservationByOrderNo(request.getTenantId(), orderNo)
        );
    }
}
