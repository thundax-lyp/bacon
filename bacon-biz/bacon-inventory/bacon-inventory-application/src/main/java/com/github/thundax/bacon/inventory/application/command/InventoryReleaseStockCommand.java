package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;

public record InventoryReleaseStockCommand(OrderNo orderNo, InventoryReleaseReason reason) {}
