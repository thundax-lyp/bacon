package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.mapper.OrderNoMapper;
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
        return inventoryApplicationService.reserveStock(TenantId.of(tenantId), OrderNoMapper.toDomain(orderNo), items);
    }

    @Override
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return inventoryApplicationService.releaseReservedStock(TenantId.of(tenantId), OrderNoMapper.toDomain(orderNo), reason);
    }

    @Override
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return inventoryApplicationService.deductReservedStock(TenantId.of(tenantId), OrderNoMapper.toDomain(orderNo));
    }
}
