package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InventoryApplicationService {

    private final InventoryReservationApplicationService reservationApplicationService;
    private final InventoryReleaseApplicationService releaseApplicationService;
    private final InventoryDeductionApplicationService deductionApplicationService;

    public InventoryApplicationService(
            InventoryReservationApplicationService reservationApplicationService,
            InventoryReleaseApplicationService releaseApplicationService,
            InventoryDeductionApplicationService deductionApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
        this.releaseApplicationService = releaseApplicationService;
        this.deductionApplicationService = deductionApplicationService;
    }

    public InventoryReservationResultDTO reserveStock(OrderNo orderNo, List<InventoryReservationItemDTO> items) {
        return reservationApplicationService.reserveStock(orderNo, items);
    }

    public InventoryReservationResultDTO releaseReservedStock(OrderNo orderNo, InventoryReleaseReason reason) {
        return releaseApplicationService.releaseReservedStock(orderNo, reason);
    }

    public InventoryReservationResultDTO deductReservedStock(OrderNo orderNo) {
        return deductionApplicationService.deductReservedStock(orderNo);
    }
}
