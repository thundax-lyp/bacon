package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryInterfaceAssembler;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryReservationResponseAssembler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class InventoryReadFacadeLocalImpl implements InventoryReadFacade {

    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryReadFacadeLocalImpl(InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Override
    public InventoryStockFacadeResponse getAvailableStock(InventoryAvailableStockFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromStockDto(
                inventoryQueryService.getAvailableStock(InventoryInterfaceAssembler.toAvailableStockQuery(request)));
    }

    @Override
    public InventoryStockListFacadeResponse batchGetAvailableStock(InventoryBatchAvailableStockFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromStockDtos(
                inventoryQueryService.batchGetAvailableStock(
                        InventoryInterfaceAssembler.toBatchAvailableStockQuery(request)));
    }

    @Override
    public InventoryReservationFacadeResponse getReservationByOrderNo(InventoryReservationGetFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromDto(
                inventoryQueryService.getReservationByOrderNo(
                        InventoryInterfaceAssembler.toReservationQuery(request)));
    }

    private void requireContext() {
        BaconContextHolder.requireTenantId();
        BaconContextHolder.requireUserId();
    }
}
