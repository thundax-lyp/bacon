package com.github.thundax.bacon.inventory.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InventoryLocalFacadeContextTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void commandFacadeShouldRequireUserId() {
        InventoryCommandFacadeLocalImpl facade =
                new InventoryCommandFacadeLocalImpl(
                        new InventoryCommandApplicationService(null, null, null, null, null, null, null));
        BaconContextHolder.set(new BaconContext(1001L, null));

        assertThatThrownBy(() -> facade.deductReservedStock(new InventoryDeductFacadeRequest("ORD-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("userId must not be null");
    }

    @Test
    void readFacadeShouldRequireUserId() {
        InventoryReadFacadeLocalImpl facade =
                new InventoryReadFacadeLocalImpl(new InventoryQueryApplicationService(null, null, null, null));
        BaconContextHolder.set(new BaconContext(1001L, null));

        assertThatThrownBy(() -> facade.getAvailableStock(new InventoryAvailableStockFacadeRequest(101L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("userId must not be null");
    }
}
