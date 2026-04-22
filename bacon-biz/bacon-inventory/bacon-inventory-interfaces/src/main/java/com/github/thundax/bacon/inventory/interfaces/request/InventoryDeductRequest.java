package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 库存扣减请求。
 */
public record InventoryDeductRequest(@NotBlank @Size(max = 64) String orderNo) {}
