package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryApplicationService {

    private final InventoryReservationApplicationService reservationApplicationService;
    private final InventoryReleaseApplicationService releaseApplicationService;
    private final InventoryDeductionApplicationService deductionApplicationService;

    public InventoryApplicationService(InventoryReservationApplicationService reservationApplicationService,
                                       InventoryReleaseApplicationService releaseApplicationService,
                                       InventoryDeductionApplicationService deductionApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
        this.releaseApplicationService = releaseApplicationService;
        this.deductionApplicationService = deductionApplicationService;
    }

    public InventoryReservationResultDTO reserveStock(TenantId tenantId, OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        return reservationApplicationService.reserveStock(tenantId, orderNo, items);
    }

    public InventoryReservationResultDTO releaseReservedStock(TenantId tenantId, OrderNo orderNo, InventoryReleaseReason reason) {
        return releaseApplicationService.releaseReservedStock(tenantId, orderNo, reason);
    }

    public InventoryReservationResultDTO deductReservedStock(TenantId tenantId, OrderNo orderNo) {
        return deductionApplicationService.deductReservedStock(tenantId, orderNo);
    }
}
