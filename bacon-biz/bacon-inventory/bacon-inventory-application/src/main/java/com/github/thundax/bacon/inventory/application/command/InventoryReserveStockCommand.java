package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import java.util.List;

public record InventoryReserveStockCommand(OrderNo orderNo, List<InventoryReservationItemCommand> items) {}
