package com.github.thundax.bacon.inventory.application.query;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import java.util.Set;

public record InventoryBatchAvailableStockQuery(Set<SkuId> skuIds) {}
