package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import java.util.List;
import org.springframework.stereotype.Service;

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

    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return reservationApplicationService.reserveStock(tenantId, orderNo, items);
    }

    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return releaseApplicationService.releaseReservedStock(tenantId, orderNo, reason);
    }

    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return deductionApplicationService.deductReservedStock(tenantId, orderNo);
    }
}
