package com.github.thundax.bacon.inventory.api.facade;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import java.util.List;

public interface InventoryCommandFacade {

    InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items);

    InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason);

    InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo);
}
