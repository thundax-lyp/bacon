package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public record InventoryAuditLogQuery(OrderNo orderNo) {}
