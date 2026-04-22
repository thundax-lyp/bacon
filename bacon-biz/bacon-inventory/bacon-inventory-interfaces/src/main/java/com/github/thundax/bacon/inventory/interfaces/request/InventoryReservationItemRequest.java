package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 库存预占请求明细。
 */
public record InventoryReservationItemRequest(@NotNull @Positive Long skuId, @NotNull @Positive Integer quantity) {}
