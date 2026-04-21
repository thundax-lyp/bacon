package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryInterfaceAssembler;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryReservationResponseAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class InventoryCommandFacadeLocalImpl implements InventoryCommandFacade {

    private final InventoryCommandApplicationService inventoryCommandApplicationService;

    public InventoryCommandFacadeLocalImpl(InventoryCommandApplicationService inventoryCommandApplicationService) {
        this.inventoryCommandApplicationService = inventoryCommandApplicationService;
    }

    @Override
    public InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.reserveStock(InventoryInterfaceAssembler.toReserveCommand(request)));
    }

    @Override
    public InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.releaseReservedStock(
                        InventoryInterfaceAssembler.toReleaseCommand(request)));
    }

    @Override
    public InventoryReservationFacadeResponse deductReservedStock(InventoryDeductFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(
                inventoryCommandApplicationService.deductReservedStock(
                        InventoryInterfaceAssembler.toDeductCommand(request)));
    }

    private void requireContext() {
        BaconContextHolder.requireTenantId();
        BaconContextHolder.requireUserId();
    }
}
