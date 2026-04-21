package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;

public record InventoryReservationItemCommand(SkuId skuId, Integer quantity) {}
