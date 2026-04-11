package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class InventoryCommandFacadeLocalImpl implements InventoryCommandFacade {

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryCommandFacadeLocalImpl(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @Override
    public InventoryReservationResultDTO reserveStock(String orderNo, List<InventoryReservationItemDTO> items) {
        requireContext();
        return inventoryApplicationService.reserveStock(OrderNoCodec.toDomain(orderNo), items);
    }

    @Override
    public InventoryReservationResultDTO releaseReservedStock(String orderNo, String reason) {
        requireContext();
        return inventoryApplicationService.releaseReservedStock(OrderNoCodec.toDomain(orderNo), toReleaseReason(reason));
    }

    private InventoryReleaseReason toReleaseReason(String reason) {
        try {
            return InventoryReleaseReason.from(reason);
        } catch (IllegalArgumentException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_RELEASE_REASON, reason);
        }
    }

    @Override
    public InventoryReservationResultDTO deductReservedStock(String orderNo) {
        requireContext();
        return inventoryApplicationService.deductReservedStock(OrderNoCodec.toDomain(orderNo));
    }

    private void requireContext() {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("tenantId must not be null");
        }
        Long userId = BaconContextHolder.currentUserId();
        if (userId == null) {
            throw new IllegalStateException("userId must not be null");
        }
    }
}
