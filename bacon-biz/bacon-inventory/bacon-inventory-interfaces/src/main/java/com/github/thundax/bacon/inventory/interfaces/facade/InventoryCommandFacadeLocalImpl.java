package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.interfaces.assembler.InventoryReservationResponseAssembler;
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
    public InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(inventoryApplicationService.reserveStock(
                OrderNoCodec.toDomain(request.getOrderNo()), toReservationItemDtos(request.getItems())));
    }

    @Override
    public InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(inventoryApplicationService.releaseReservedStock(
                OrderNoCodec.toDomain(request.getOrderNo()), toReleaseReason(request.getReason())));
    }

    private InventoryReleaseReason toReleaseReason(String reason) {
        try {
            return InventoryReleaseReason.from(reason);
        } catch (IllegalArgumentException ex) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_RELEASE_REASON, reason);
        }
    }

    @Override
    public InventoryReservationFacadeResponse deductReservedStock(InventoryDeductFacadeRequest request) {
        requireContext();
        return InventoryReservationResponseAssembler.fromResult(
                inventoryApplicationService.deductReservedStock(OrderNoCodec.toDomain(request.getOrderNo())));
    }

    private void requireContext() {
        BaconContextHolder.requireTenantId();
        BaconContextHolder.requireUserId();
    }

    private List<InventoryReservationItemDTO> toReservationItemDtos(List<InventoryReservationItemFacadeRequest> items) {
        return items.stream()
                .map(item -> new InventoryReservationItemDTO(item.getSkuId(), item.getQuantity()))
                .toList();
    }
}
