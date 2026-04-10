package com.github.thundax.bacon.inventory.api.facade;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import java.util.List;
import java.util.Set;

public interface InventoryReadFacade {

    InventoryStockDTO getAvailableStock(Long skuId);

    List<InventoryStockDTO> batchGetAvailableStock(Set<Long> skuIds);

    InventoryReservationDTO getReservationByOrderNo(String orderNo);
}
