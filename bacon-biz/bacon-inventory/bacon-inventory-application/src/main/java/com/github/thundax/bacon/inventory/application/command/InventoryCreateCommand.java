package com.github.thundax.bacon.inventory.application.command;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;

public record InventoryCreateCommand(SkuId skuId, Integer onHandQuantity, InventoryStatus status) {}
