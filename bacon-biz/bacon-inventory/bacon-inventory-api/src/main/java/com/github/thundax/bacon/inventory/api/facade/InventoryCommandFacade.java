package com.github.thundax.bacon.inventory.api.facade;

import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;

public interface InventoryCommandFacade {

    InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest request);

    InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest request);

    InventoryReservationFacadeResponse deductReservedStock(InventoryDeductFacadeRequest request);
}
