package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.application.service.InventoryQueryService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InventoryReadFacadeLocalImpl implements InventoryReadFacade {

    private final InventoryQueryService inventoryQueryService;

    public InventoryReadFacadeLocalImpl(InventoryQueryService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Override
    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return inventoryQueryService.getAvailableStock(tenantId, skuId);
    }

    @Override
    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(tenantId, skuIds);
    }

    @Override
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(tenantId, orderNo);
    }
}
