package com.github.thundax.bacon.inventory.interfaces.provider;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.application.service.InventoryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class InventoryCommandFacadeLocalImpl implements InventoryCommandFacade {

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryCommandFacadeLocalImpl(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @Override
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return inventoryApplicationService.reserveStock(tenantId, orderNo, items);
    }

    @Override
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return inventoryApplicationService.releaseReservedStock(tenantId, orderNo, reason);
    }

    @Override
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return inventoryApplicationService.deductReservedStock(tenantId, orderNo);
    }
}
