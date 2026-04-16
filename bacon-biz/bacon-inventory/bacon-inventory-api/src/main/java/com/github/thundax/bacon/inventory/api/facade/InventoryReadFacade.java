package com.github.thundax.bacon.inventory.api.facade;

import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;

public interface InventoryReadFacade {

    InventoryStockFacadeResponse getAvailableStock(InventoryAvailableStockFacadeRequest request);

    InventoryStockListFacadeResponse batchGetAvailableStock(InventoryBatchAvailableStockFacadeRequest request);

    InventoryReservationFacadeResponse getReservationByOrderNo(InventoryReservationGetFacadeRequest request);
}
