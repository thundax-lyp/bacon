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
    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return inventoryQueryService.getAvailableStock(TenantId.of(tenantId), SkuIdMapper.toDomain(skuId));
    }

    @Override
    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return inventoryQueryService.batchGetAvailableStock(
                TenantId.of(tenantId),
                skuIds == null
                        ? Set.of()
                        : skuIds.stream().map(SkuIdMapper::toDomain).collect(Collectors.toSet()));
    }

    @Override
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return inventoryQueryService.getReservationByOrderNo(TenantId.of(tenantId), OrderNoCodec.toDomain(orderNo));
    }
}
