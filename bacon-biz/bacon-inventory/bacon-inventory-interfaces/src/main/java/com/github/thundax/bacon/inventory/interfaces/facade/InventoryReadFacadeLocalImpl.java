package com.github.thundax.bacon.inventory.interfaces.facade;

import com.github.thundax.bacon.common.commerce.mapper.SkuIdMapper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class InventoryReadFacadeLocalImpl implements InventoryReadFacade {

    private final InventoryQueryApplicationService inventoryQueryService;

    public InventoryReadFacadeLocalImpl(InventoryQueryApplicationService inventoryQueryService) {
        this.inventoryQueryService = inventoryQueryService;
    }

    @Override
    public InventoryStockDTO getAvailableStock(Long skuId) {
        return inventoryQueryService.getAvailableStock(SkuIdMapper.toDomain(skuId));
    }

    @Override
    public List<InventoryStockDTO> batchGetAvailableStock(Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(
                skuIds == null ? Set.of() : skuIds.stream().map(SkuIdMapper::toDomain).collect(Collectors.toSet()));
    }

    @Override
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return com.github.thundax.bacon.common.core.context.BaconContextHolder.callWithTenantId(
                tenantId,
                () -> inventoryQueryService.getReservationByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo)));
    }
}
