package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public record InventoryDeductStockCommand(OrderNo orderNo) {}
