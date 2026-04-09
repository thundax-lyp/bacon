package com.github.thundax.bacon.inventory.api.facade;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import java.util.List;
import java.util.Set;

public interface InventoryReadFacade {

    InventoryStockDTO getAvailableStock(Long tenantId, Long skuId);

    List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds);

    InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo);
}
